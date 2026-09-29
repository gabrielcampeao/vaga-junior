package com.gabrielcampeao.posto.web;

import com.gabrielcampeao.posto.domain.Abastecimento;
import com.gabrielcampeao.posto.service.AbastecimentoService;
import com.gabrielcampeao.posto.web.dto.AbastecimentoRequest;
import com.gabrielcampeao.posto.web.dto.AbastecimentoResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/abastecimentos")
public class AbastecimentoController {

    private final AbastecimentoService service;

    public AbastecimentoController(AbastecimentoService service) {
        this.service = service;
    }

    @GetMapping
    public Page<AbastecimentoResponse> pesquisar(
            @RequestParam(required = false) Long bombaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            Pageable pageable) {
        return service.pesquisar(bombaId, inicio, fim, pageable).map(AbastecimentoResponse::fromEntity);
    }

    @GetMapping("/{id}")
    public AbastecimentoResponse buscarPorId(@PathVariable Long id) {
        return AbastecimentoResponse.fromEntity(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AbastecimentoResponse> registrar(@Valid @RequestBody AbastecimentoRequest request) {
        Abastecimento salvo = service.registrar(request.bombaId(), request.litros(), request.dataHora());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(salvo.getId())
                .toUri();

        return ResponseEntity.created(location).body(AbastecimentoResponse.fromEntity(salvo));
    }

    @PutMapping("/{id}")
    public AbastecimentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody AbastecimentoRequest request) {
        Abastecimento atualizado = service.atualizar(id, request.bombaId(), request.litros(), request.dataHora());
        return AbastecimentoResponse.fromEntity(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
