# Cadastro administrativo de espaços

Primeira entrega do CTH-15: cadastro, edição, consulta paginada e ativação/desativação.
Todos os endpoints exigem sessão de administrador ativo com `MANAGE_SPACES`. Escritas
também exigem o token CSRF, conforme [Acesso administrativo](15-autenticacao.md).

## Unidade cadastrada

Cada registro representa uma sala, mesa compartilhada ou estação individual,
identificado por UUID. Salas diferentes podem ter o mesmo nome: a identificação é
feita pelo UUID. Cada estação pode ser cadastrada separadamente, por exemplo
`Estação 01`. Vínculo à sala, numeração e posição no mapa usam as APIs descritas em
[Estações e mapa](20-estacoes-e-mapa.md).

| Campo | Regra |
|---|---|
| `name` | Obrigatório, até 150 caracteres; espaços nas extremidades são removidos |
| `type` | `ROOM`, `SHARED_TABLE` ou `WORKSTATION` |
| `capacity` | Inteiro positivo; `WORKSTATION` aceita somente uma pessoa |
| `description` | Opcional, até 2000 caracteres; espaços nas extremidades são removidos |
| `active` | Começa como `true`; alterado por endpoint próprio |
| `id`, `createdAt` | Gerados pelo backend; a data é retornada como instante em UTC |
| `stationPlacement` | Posição estruturada da estação; ausente quando não configurada; alterada por API própria |

`active` indica se o cadastro está habilitado para uso nas próximas etapas de catálogo
e reservas. Não representa disponibilidade de horário. Desativar preserva o registro,
seu UUID e seus dados; não há exclusão física pela API.

## Endpoints

| Método | Endpoint | Resultado |
|---|---|---|
| POST | `/api/v1/admin/spaces` | Cria ativo; responde 201, corpo e cabeçalho `Location` |
| GET | `/api/v1/admin/spaces` | Lista paginada, incluindo ativos e inativos por padrão |
| GET | `/api/v1/admin/spaces/{id}` | Consulta o cadastro, inclusive inativo |
| PUT | `/api/v1/admin/spaces/{id}` | Substitui nome, tipo, capacidade e descrição; mantém situação e identidade |
| PATCH | `/api/v1/admin/spaces/{id}/status` | Ativa ou desativa; responde com o cadastro atualizado |

Corpo de criação/edição:

```json
{
  "name": "Sala de reunião",
  "type": "ROOM",
  "capacity": 8,
  "description": "Sala com projetor."
}
```

Na edição, omitir `description` ou enviar `null` limpa a descrição. Os demais campos
desse corpo são obrigatórios. A edição preserva `stationPlacement`. Salas com estações
vinculadas e estações posicionadas não podem mudar de tipo (409). Para desativar:

```json
{"active": false}
```

Para reativar, envie `{"active": true}`. Repetir o mesmo estado é permitido.
Os tipos JSON são verificados sem conversão automática de escalares: capacidade
fracionária, tipo numérico e situação numérica são rejeitados com 400.

## Paginação e erros

O GET de listagem aceita `page` (a partir de 0, padrão 0), `size` (1 a 100, padrão 20)
e `active` opcional (`true` ou `false`). A ordenação é fixa por nome e UUID, garantindo
desempate entre nomes iguais. Exemplo:

```text
GET /api/v1/admin/spaces?active=true&page=0&size=20
```

A resposta contém `items`, `page`, `size`, `totalElements` e `totalPages`. Uma página
além dos resultados retorna lista vazia. Valores de paginação inválidos retornam 400.

Entradas inválidas retornam 400; UUID válido sem cadastro retorna 404. Falhas de
validação usam o [tratamento centralizado com ProblemDetail](14-erros-da-api.md).
Uma estação com capacidade diferente de 1 gera erro de validação
`capacityCompatible`. A capacidade também é protegida por restrição no PostgreSQL.

## Persistência e próximas partes

A migração `V3__create_spaces.sql` cria `spaces`, suas restrições e o índice para
ordenação. V1 e V2 permanecem preservadas. Esta entrega não cadastra espaços reais
automaticamente; nomes e capacidades serão informados pela administração.

O CTH-15 permanece parcialmente implementado. As próximas partes incluem foto,
recursos/equipamentos descritos na sala, regras e avisos, a interface do mapa e controle
de visibilidade pública. Catálogo público,
disponibilidade por data e reservas pertencem às etapas seguintes.

O funcionamento do Hub já tem APIs próprias, descritas em
[Funcionamento do Hub](19-funcionamento-do-hub.md); sua tela de edição ainda será entregue.
As APIs de organização e consulta pública das estações estão em [Estações e mapa](20-estacoes-e-mapa.md).
