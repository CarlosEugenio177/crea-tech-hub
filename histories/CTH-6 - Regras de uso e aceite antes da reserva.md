# CTH-6 - Regras de uso e aceite antes da reserva

| Campo | Valor |
|---|---|
| **Prioridade** | Alta (MVP) |
| **Requisitos** | RF06 · RN03 |
| **Depende de** | CTH-15 (textos e regras por ambiente) |

## Objetivo

Garantir que o usuário conheça e aceite as regras do ambiente antes de confirmar a reserva.

## Quem usa

| Perfil | O que faz |
|---|---|
| Mutualista / profissional do Crea | Lê e aceita as regras |
| Instituição parceira | Lê e aceita as regras |
| Público externo | Lê e aceita as regras |
| Administrador / operador | Mantém os textos de regras (CTH-15) |

## Indicadores de sucesso (proposta da equipe)

- 100% das reservas confirmadas com aceite das regras registrado.

## História

**Como** usuário, **quero** ver as regras do ambiente antes de confirmar, **para** saber o que é permitido e me comprometer com o uso correto.

## Critérios de aceite

- [x] A consulta pública das regras gerais do Hub está disponível por API.
- [x] O formulário **exibe as regras gerais de uso** do Hub antes do envio.
- [x] O usuário precisa **aceitar** as regras para concluir; sem aceite, a reserva **não é registrada**.
- [x] O horário do **aceite fica registrado** junto à reserva.
- [ ] As regras aparecem de forma legível no celular e acessível por teclado (CTH-21).

### Verificação manual

Use a coleção [Regras públicas de uso](../docs/postman/public-usage-rules.postman_collection.json)
para consultar o endpoint público sem autenticação e validar o formato da resposta.
