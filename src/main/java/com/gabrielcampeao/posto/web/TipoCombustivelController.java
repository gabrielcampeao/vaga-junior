package com.gabrielcampeao.posto.web;

import com.gabrielcampeao.posto.domain.TipoCombustivel;
import com.gabrielcampeao.posto.service.TipoCombustivelService;
import com.gabrielcampeao.posto.web.dto.TipoCombustivelRequest;
import com.gabrielcampeao.posto.web.dto.TipoCombustivelResponse;
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
@RequestMapping("/api/tipos-combustivel")
public class TipoCombustivelController {

    private final TipoCombustivelService service;

    public TipoCombustivelController(TipoCombustivelService service) {
        this.service = service;
    }

    @GetMapping
    public Page<TipoCombustivelResponse> listar(Pageable pageable) {
        return service.listar(pageable).map(TipoCombustivelResponse::fromEntity);
    }

    @GetMapping("/{id}")
    public TipoCombustivelResponse buscar(@PathVariable Long id) {
        return TipoCombustivelResponse.fromEntity(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<TipoCombustivelResponse> criar(@Valid @RequestBody TipoCombustivelRequest request) {
        TipoCombustivel tipo = new TipoCombustivel(request.nome(), request.precoLitro());
        TipoCombustivel salvo = service.criar(tipo);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(salvo.getId())
                .toUri();

        return ResponseEntity.created(location).body(TipoCombustivelResponse.fromEntity(salvo));
    }

    @PutMapping("/{id}")
    public TipoCombustivelResponse atualizar(@PathVariable Long id,
                                             @Valid @RequestBody TipoCombustivelRequest request) {
        TipoCombustivel dados = new TipoCombustivel(request.nome(), request.precoLitro());
        return TipoCombustivelResponse.fromEntity(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
