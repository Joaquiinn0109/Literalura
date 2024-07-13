package com.aluracursos.literalura.service;

import com.aluracursos.literalura.model.Autor;
import com.aluracursos.literalura.repository.AutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutorService {
    @Autowired
    private AutorRepository autorRepository;

    public List<Autor> obtenerTodos() {
        return autorRepository.findAll();
    }

    public List<Autor> obtenerAutoresVivosEnAnio(Integer anio) {
        return autorRepository.findByFechaDeNacimientoLessThanEqualAndFechaDeMuerteGreaterThanEqual(anio, anio);
    }

    public Autor guardar(Autor autor) {
        return autorRepository.save(autor);
    }
}
