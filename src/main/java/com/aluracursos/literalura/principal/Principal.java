package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.model.Datos;
import com.aluracursos.literalura.model.DatosLibros;
import com.aluracursos.literalura.model.Libro;
import com.aluracursos.literalura.model.Autor;
import com.aluracursos.literalura.service.ConsumoAPI;
import com.aluracursos.literalura.service.ConvierteDatos;
import com.aluracursos.literalura.service.LibroService;
import com.aluracursos.literalura.service.AutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component
public class Principal {

    private static final String URL_BASE = "https://gutendex.com/books/";

    private final LibroService libroService;
    private final AutorService autorService;

    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    private final ConvierteDatos conversor = new ConvierteDatos();
    private final Scanner teclado = new Scanner(System.in);

    @Autowired
    public Principal(LibroService libroService, AutorService autorService) {
        this.libroService = libroService;
        this.autorService = autorService;
    }

    public void muestraMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar libros por titulos
                    2 - Mostrar libros registrados
                    3 - Mostrar autores registrados
                    4 - Mostrar autores vivos en un determinado año
                    5 - Mostrar libros por idiomas
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarLibroWeb();
                    break;
                case 2:
                    mostrarLibrosRegistrados();
                    break;
                case 3:
                    mostrarAutoresRegistrados();
                    break;
                case 4:
                    mostrarAutoresVivosEnAnio();
                    break;
                case 5:
                    mostrarLibrosPorIdiomas();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida, por favor ingrese una opcion valida.");
            }
        }
    }

    private List<DatosLibros> getDatosLibros(){
        System.out.println("Escribe el nombre del libro que deseas buscar:");
        var nombreLibro = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + nombreLibro.replace(" ", "+"));
        Datos datos = conversor.obtenerDatos(json, Datos.class);
        return datos.resultados();
    }

    private void buscarLibroWeb() {
        var datosLibros = getDatosLibros();
        if (datosLibros.isEmpty()) {
            System.out.println("No se encontraron libros con ese título.");
            return;
        }
        var libro = datosLibros.get(0);
        System.out.println(libro);

        Libro libroEntidad = new Libro();
        libroEntidad.setTitulo(libro.titulo());
        libroEntidad.setIdioma(libro.idiomas().get(0));
        libroEntidad.setNumeroDescargas(libro.numeroDescargas());

        Autor autorEntidad = new Autor();
        autorEntidad.setNombre(libro.autor().get(0).nombre());
        autorEntidad.setFechaDeNacimiento(libro.autor().get(0).fechaDeNacimiento());
        autorEntidad.setFechaDeMuerte(libro.autor().get(0).fechaDeMuerte());

        libroEntidad.setAutor(autorEntidad);

        autorService.guardar(autorEntidad);  // Guardar autor primero
        libroService.guardar(libroEntidad);  // Luego guardar el libro

        System.out.println("Libro guardado exitosamente.");
    }

    private void mostrarLibrosRegistrados() {
        var libros = libroService.obtenerTodos();
        libros.forEach(l -> System.out.println("-------- Libro --------" +
                "\nTitulo: " + l.getTitulo() + "\nIdioma: " + l.getIdioma() + "\nAutor: " +l.getAutor().getNombre() +
                "\nNumero de Descargas: " + l.getNumeroDescargas() + "\n-----------------\n"));
    }

    private void mostrarAutoresRegistrados() {
        var autores = autorService.obtenerTodos();
        autores.forEach(a -> {
            String libros = a.getLibros().stream()
                    .map(Libro::getTitulo)
                    .collect(Collectors.joining(", "));
            System.out.println("Nombre Autor: " + a.getNombre() +
                    "\nFecha de Nacimiento: " + a.getFechaDeNacimiento() +
                    "\nFecha de Muerte: " + a.getFechaDeMuerte() +
                    "\nLibros: " + libros + "\n");
        });
    }

    private void mostrarAutoresVivosEnAnio() {
        System.out.println("Ingrese el año:");
        var anio = teclado.nextInt();
        teclado.nextLine();
        var autores = autorService.obtenerAutoresVivosEnAnio(anio);
        autores.forEach(a -> {
            String libros = a.getLibros().stream()
                    .map(Libro::getTitulo)
                    .collect(Collectors.joining(", "));
            System.out.println("Nombre Autor: " + a.getNombre() +
                    "\nFecha de Nacimiento: " + a.getFechaDeNacimiento() +
                    "\nFecha de Muerte: " + a.getFechaDeMuerte() +
                    "\nLibros: " + libros + "\n");
        });
    }

    private void mostrarLibrosPorIdiomas() {
        System.out.println("Ingrese el idioma:");
        System.out.println("""
                es - Español
                en - Inglés
                fr - Frances
                hu - Hungaro
                """);
        var idioma = teclado.nextLine();
        var libros = libroService.buscarPorIdioma(idioma);
        libros.forEach(l -> System.out.println("-------- Libro --------" +
                "\nTitulo: " + l.getTitulo() + "\nIdioma: " + l.getIdioma() + "\nAutor: " +l.getAutor().getNombre() +
                "\nNumero de Descargas: " + l.getNumeroDescargas() + "\n-----------------\n"));
    }
}
