package com.gabrielcampeao.posto.web;

import com.gabrielcampeao.posto.domain.Bomba;
import com.gabrielcampeao.posto.service.BombaService;
import com.gabrielcampeao.posto.web.dto.BombaRequest;
import com.gabrielcampeao.posto.web.dto.BombaResponse;
import com.gabrielcampeao.posto.web.dto.ErroResponse;
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

@Tag(name = "Bombas", description = "Endpoints de gerenciamento de bombas de combustível")
@RestController
@RequestMapping("/api/bombas")
public class BombaController {

    private final BombaService service;

    public BombaController(BombaService service) {
        this.service = service;
    }

    @Operation(summary = "Listar bombas de forma paginada")
    @GetMapping
    public Page<BombaResponse> listar(Pageable pageable) {
        return service.listar(pageable).map(BombaResponse::fromEntity);
    }

    @Operation(summary = "Buscar bomba por ID", responses = {
            @ApiResponse(responseCode = "200", description = "Bomba encontrada"),
            @ApiResponse(responseCode = "404", description = "Bomba não encontrada", content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/{id}")
    public BombaResponse buscar(@PathVariable Long id) {
        return BombaResponse.fromEntity(service.buscarPorId(id));
    }

    @Operation(summary = "Cadastrar nova bomba", responses = {
            @ApiResponse(responseCode = "201", description = "Bomba cadastrada"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Tipo de combustível informado não existe"),
            @ApiResponse(responseCode = "409", description = "Identificador de bomba já existente", content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @PostMapping
    public ResponseEntity<BombaResponse> criar(@Valid @RequestBody BombaRequest request) {
        Bomba bomba = service.criar(request.identificador(), request.tipoCombustivelId());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(bomba.getId())
                .toUri();

        return ResponseEntity.created(location).body(BombaResponse.fromEntity(bomba));
    }

    @Operation(summary = "Atualizar bomba por ID", responses = {
            @ApiResponse(responseCode = "200", description = "Bomba atualizada"),
            @ApiResponse(responseCode = "404", description = "Bomba ou tipo de combustível não encontrado"),
            @ApiResponse(responseCode = "409", description = "Identificador já pertence a outra bomba")
    })
    @PutMapping("/{id}")
    public BombaResponse atualizar(@PathVariable Long id, @Valid @RequestBody BombaRequest request) {
        Bomba salva = service.atualizar(id, request.identificador(), request.tipoCombustivelId());
        return BombaResponse.fromEntity(salva);
    }

    @Operation(summary = "Excluir bomba por ID", responses = {
            @ApiResponse(responseCode = "204", description = "Bomba excluída"),
            @ApiResponse(responseCode = "404", description = "Bomba não encontrada"),
            @ApiResponse(responseCode = "409", description = "Não é possível excluir bomba que possui abastecimentos vinculados", content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
