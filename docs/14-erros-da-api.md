# Erros da API

`GlobalExceptionHandler`, em `shared.error`, centraliza as exceções da camada MVC
com `@RestControllerAdvice`. As respostas usam `ProblemDetail` (RFC 9457), com
`Content-Type: application/problem+json`.

| Situação | HTTP |
|---|---|
| `ResourceNotFoundException` | 404 |
| `ResourceConflictException` | 409 |
| Dados de entrada inválidos | 400 |
| Corpo ausente ou JSON inválido | 400 |
| Falha de autenticação lançada durante o processamento MVC | 401 |
| Acesso negado durante o processamento MVC | 403 |
| Falha inesperada | 500 |

Exemplo de resposta:

```json
{
  "title": "Not Found",
  "status": 404,
  "detail": "Usuário não encontrado.",
  "instance": "/api/v1/users/123"
}
```

Erros de validação incluem `errors`, com `field` e `message` para cada falha.
Os valores rejeitados não são incluídos. As mensagens das exceções de recurso
devem ser apropriadas para exibição ao solicitante.

O tratamento padrão do Spring MVC preserva os demais status e cabeçalhos HTTP,
como `Allow` em respostas 405. Falhas 5xx recebem mensagem genérica e são registradas
no log do servidor; detalhes internos permanecem fora da resposta.

Exceções nos filtros do Spring Security são tratadas em `SecurityConfiguration`,
antes da camada MVC. A configuração atual mantém somente a leitura da saúde pública.
Falhas de inicialização, como conexão com banco e migrações, também ficam fora do
escopo do advice.
