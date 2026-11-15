# CTH-15 - Cadastro de ambientes e disponibilidade ao público

| Campo | Valor |
|---|---|
| **Prioridade** | Alta (MVP) |
| **Requisitos** | RF15 · RF17 · [Ambientes e recursos](../docs/03-ambientes-e-recursos.md) |
| **Depende de** | CTH-12 |

## Objetivo

Permitir que a equipe mantenha o catálogo do Hub: ambientes, estações, equipamentos, capacidades, regras e avisos, e controle quais estão disponíveis ao público.

## Quem usa

| Perfil | O que faz |
|---|---|
| Administrador / operador | Cadastra e edita ambientes, estações, equipamentos, regras e avisos |
| Diretoria | Solicita a liberação ou restrição de salas da diretoria |

## Indicadores de sucesso (proposta da equipe)

- Ambientes e regras atualizados pela equipe sem apoio do time de desenvolvimento.

## História

**Como** administrador, **quero** cadastrar e editar ambientes, estações, equipamentos, capacidades, regras e textos de aviso, e torná-los disponíveis ou não ao público, **para** manter o sistema fiel à realidade do Hub.

## Critérios de aceite

**Entrega parcial:** API administrativa de cadastro básico, edição, consulta paginada
e ativação/desativação implementada. Ver [contrato de espaços](../docs/16-espacos.md).
Os critérios abaixo continuam abertos até a entrega dos campos e comportamentos
completos, incluindo foto, recursos, mapa, funcionamento e integração pública.

- [ ] Posso **cadastrar e editar** ambientes e estações (foco em salas; reserva de equipamentos fora do escopo atual).
- [ ] Cada cadastro tem **nome**, **foto**, **capacidade**, **recursos** e um **campo opcional** para listar os **equipamentos disponíveis** na sala.
- [ ] Trava de **mínimo de pessoas**, **máximo de horas** e textos específicos por sala não são obrigatórios por enquanto.
- [ ] Para salas com estações (sala de estudos, coworking), cadastro as **estações numeradas** e a posição delas no **mapa** (conforme Framer).
- [ ] Posso configurar o **horário de funcionamento do Hub** (padrão **8h às 18h**) e os **dias da semana de funcionamento** (padrão **segunda a sexta**), alteráveis quando necessário.
- [ ] Posso tornar um ambiente **disponível ou indisponível ao público** (ex.: salas da diretoria).
- [ ] Ambientes indisponíveis **somem do catálogo e da reserva pública** (CTH-1, CTH-2), sem apagar o histórico de reservas.
- [ ] As alterações valem para **novas reservas**; reservas já feitas não mudam sem decisão da equipe.
