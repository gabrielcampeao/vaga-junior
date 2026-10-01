# Posto: API de vendas de combustível

Esse é o projeto que fiz para o desafio técnico. É uma API para controlar um posto: com ela você cadastra combustíveis e bombas, registra abastecimentos e vê quanto foi vendido em cada período.

Feito com Java 21, Spring Boot 3, Spring Data JPA e banco H2.

## Como rodar

Você só precisa do JDK 21. O Maven já vem no projeto.

```bash
./mvnw spring-boot:run
```

Com a aplicação no ar, abra o Swagger:

http://localhost:8080/swagger-ui.html

Todas as rotas estão lá, e você testa cada uma pelo próprio navegador.

O banco fica salvo na pasta `data`. Na primeira vez que a aplicação sobe, ela já cadastra gasolina, etanol e diesel, cada um com a sua bomba, então dá para começar a testar sem preparar nada.

## Como rodar os testes

```bash
./mvnw clean verify
```

Os testes usam um banco separado, em memória, e não mexem nos seus dados.

São uns 47 no total. Parte deles confere a conta de litros e valor, com o arredondamento nas bordas. O resto sobe a API de verdade e testa as rotas: o cálculo feito no servidor, os filtros, o resumo, as regras de nome repetido e de exclusão bloqueada, e cada formato de erro que a API devolve.

## Rotas

* `/api/tipos-combustivel` para os combustíveis
* `/api/bombas` para as bombas
* `/api/abastecimentos` para os abastecimentos
* `/api/abastecimentos/resumo` para o total vendido por combustível

As três primeiras têm listar, buscar por id, cadastrar, editar e excluir.

## Um teste rápido

1. Registre um abastecimento em `POST /api/abastecimentos` com `{"bombaId": 1, "litros": 10}`. A API calcula o valor sozinha.
2. Agora mande `{"bombaId": 1, "valor": 100}`. Dessa vez ela calcula os litros.
3. Mude o preço da gasolina em `PUT /api/tipos-combustivel/1`. Os abastecimentos que já existiam continuam com o preço antigo.
4. Tente apagar a bomba 1 em `DELETE /api/bombas/1`. A API não deixa, porque ela já tem vendas.
5. Veja o resumo em `GET /api/abastecimentos/resumo`.

## Regras do sistema

Quem abastece informa os litros ou o valor em reais, nunca os dois. O sistema calcula o resto com o preço da bomba.

Cada abastecimento guarda o preço do dia. Se o combustível ficar mais caro amanhã, as vendas de hoje não mudam.

Um combustível que está em uma bomba não pode ser apagado, e uma bomba que já vendeu também não. Nomes repetidos são recusados, mesmo escritos com maiúsculas ou espaços diferentes.

Cada abastecimento pode ter no máximo 200 litros. Esse limite fica no `application.yml`, então muda sem mexer no código.

O resumo mostra os últimos 30 dias, a não ser que você informe as datas.

Quando algo dá errado, a API responde sempre no mesmo formato, com uma mensagem em português explicando o problema.

## O que eu faria com mais tempo

Trocaria o H2 por PostgreSQL e colocaria tudo num Docker. Também usaria o Flyway para controlar as mudanças no banco e adicionaria login, porque hoje qualquer pessoa pode alterar os dados.
