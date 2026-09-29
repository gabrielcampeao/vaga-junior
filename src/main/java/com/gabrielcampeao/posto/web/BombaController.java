package com.gabrielcampeao.posto.web;

import com.gabrielcampeao.posto.domain.Bomba;
import com.gabrielcampeao.posto.service.BombaService;
import com.gabrielcampeao.posto.web.dto.BombaRequest;
import com.gabrielcampeao.posto.web.dto.BombaResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/bombas")
public class BombaController {

    private final BombaService service;

    public BombaController(BombaService service) {
        this.service = service;
    }

    @GetMapping
    public Page<BombaResponse> listar(Pageable pageable) {
        return service.listar(pageable).map(BombaResponse::fromEntity);
    }

    @GetMapping("/{id}")
    public BombaResponse buscar(@PathVariable Long id) {
        return BombaResponse.fromEntity(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<BombaResponse> criar(@Valid @RequestBody BombaRequest request) {
        Bomba bomba = service.criar(request.identificador(), request.tipoCombustivelId());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(bomba.getId())
                .toUri();

        return ResponseEntity.created(location).body(BombaResponse.fromEntity(bomba));
    }

    @PutMapping("/{id}")
    public BombaResponse atualizar(@PathVariable Long id, @Valid @RequestBody BombaRequest request) {
        Bomba salva = service.atualizar(id, request.identificador(), request.tipoCombustivelId());
        return BombaResponse.fromEntity(salva);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
