package com.gabrielcampeao.posto.web;

import com.gabrielcampeao.posto.domain.TipoCombustivel;
import com.gabrielcampeao.posto.service.TipoCombustivelService;
import com.gabrielcampeao.posto.web.dto.ErroResponse;
import com.gabrielcampeao.posto.web.dto.TipoCombustivelRequest;
import com.gabrielcampeao.posto.web.dto.TipoCombustivelResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Tipos de Combustível", description = "Endpoints de gerenciamento de combustível")
@RestController
@RequestMapping("/api/tipos-combustivel")
public class TipoCombustivelController {

    private final TipoCombustivelService service;

    public TipoCombustivelController(TipoCombustivelService service) {
        this.service = service;
    }

    @Operation(summary = "Listar combustíveis de forma paginada")
    @GetMapping
    public Page<TipoCombustivelResponse> listar(Pageable pageable) {
        return service.listar(pageable).map(TipoCombustivelResponse::fromEntity);
    }

    @Operation(summary = "Buscar combustível por ID", responses = {
            @ApiResponse(responseCode = "200", description = "Combustível encontrado"),
            @ApiResponse(responseCode = "404", description = "Combustível não encontrado", content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/{id}")
    public TipoCombustivelResponse buscar(@PathVariable Long id) {
        return TipoCombustivelResponse.fromEntity(service.buscarPorId(id));
    }

    @Operation(summary = "Cadastrar novo combustível", responses = {
            @ApiResponse(responseCode = "201", description = "Combustível cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Nome de combustível já cadastrado", content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
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

    @Operation(summary = "Atualizar combustível", responses = {
            @ApiResponse(responseCode = "200", description = "Combustível atualizado"),
            @ApiResponse(responseCode = "404", description = "Combustível não encontrado"),
            @ApiResponse(responseCode = "409", description = "Nome já cadastrado para outro combustível")
    })
    @PutMapping("/{id}")
    public TipoCombustivelResponse atualizar(@PathVariable Long id,
                                             @Valid @RequestBody TipoCombustivelRequest request) {
        TipoCombustivel dados = new TipoCombustivel(request.nome(), request.precoLitro());
        return TipoCombustivelResponse.fromEntity(service.atualizar(id, dados));
    }

    @Operation(summary = "Excluir combustível por ID", responses = {
            @ApiResponse(responseCode = "204", description = "Combustível excluído"),
            @ApiResponse(responseCode = "404", description = "Combustível não encontrado"),
            @ApiResponse(responseCode = "409", description = "Não é possível excluir combustível associado a uma bomba cadastrada", content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
