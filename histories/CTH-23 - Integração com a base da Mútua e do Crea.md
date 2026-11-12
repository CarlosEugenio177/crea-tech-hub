# CTH-23 - Integração com a base da Mútua e do Crea

| Campo | Valor |
|---|---|
| **Prioridade** | Futura (fora do MVP) |
| **Requisitos** | RF22 · RNF07 · [Escopo](../docs/02-escopo.md) |
| **Depende de** | CTH-7, CTH-17 |

## Objetivo

Validar automaticamente o cadastro do solicitante (mutualista e registro no Crea), substituindo a conferência manual.

## Quem usa

| Perfil | O que faz |
|---|---|
| Mutualista / profissional do Crea | Tem o cadastro validado sem espera |
| Equipe administrativa da Mútua-PI | Deixa de conferir manualmente |

## Indicadores de sucesso (proposta da equipe)

- Redução das conferências manuais de cadastro.

## História

**Como** equipe da Mútua-PI, **quero** que o sistema valide o cadastro direto na base da Mútua e do Crea, **para** eliminar a conferência manual.

## Critérios de aceite

- [ ] O sistema consulta a base da Mútua/Crea a partir dos dados informados (ex.: RNP).
- [ ] A situação do solicitante (CTH-17) é **preenchida automaticamente**.
- [ ] A **conferência manual continua disponível** para casos que a integração não resolve.
- [ ] A integração **não bloqueia** a reserva quando a base estiver fora do ar.
