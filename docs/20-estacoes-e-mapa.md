# Estações numeradas e mapa da sala

## Disponibilidade pública

`GET /api/v1/spaces/{id}/availability` recebe `startsAt` e `endsAt` em
horário local do Hub (por exemplo, `2027-03-10T09:15`). A consulta aceita
intervalos no mesmo dia e aplica o funcionamento e os bloqueios ativos. O fim
é exclusivo. Bloquear a sala afeta todas as estações e bloquear uma estação
afeta apenas aquela vaga.

O visitante pode escolher no mapa, pelo número ou pedir a primeira estação
livre. Consultar ou escolher não segura o horário. Ao registrar o pedido,
o backend verifica de novo a disponibilidade e os pedidos pendentes ou
confirmados ocupam seus respectivos intervalos.

`GET /api/v1/spaces/recommendations` recebe `participants` (inteiro positivo),
`startsAt` e `endsAt` no mesmo formato local do Hub. A consulta considera
somente ambientes ativos disponíveis no intervalo (excluindo bloqueios ativos e
reservas `PENDING` ou `CONFIRMED`), respeita o funcionamento do Hub e ordena os
ambientes elegíveis pela menor capacidade suficiente para o grupo, destacando o
primeiro item em `recommended` e listando todos os disponíveis em `spaces`.
Ambientes `PER_STATION` atendem reservas individuais (`participants = 1`) quando
houver ao menos uma estação ativa livre; ambientes `WHOLE_SPACE` atendem grupos
até sua `capacity` (sem trava de mínimo de pessoas nesta fase). Quando o Hub
estiver fechado (`CLOSED`) ou nenhum ambiente atender (`UNAVAILABLE`), `spaces`
retorna vazio acompanhado de `message` orientando ajustar data, horário ou
número de pessoas.

## Cadastro e posição

Esta parte do CTH-15 organiza as estações existentes dentro de salas. Cada estação
continua sendo um espaço `WORKSTATION`, de capacidade 1 e UUID próprio; uma sala é
um espaço `ROOM`. Não há duplicação de cadastros ou reservas nesta entrega.

## Configurar uma estação

O ambiente de destino precisa estar em modalidade `PER_STATION`. A geração em lote
e o editor visual estão em [Montagem de salas](22-montagem-de-salas.md).

Crie a sala e as estações com o cadastro administrativo de espaços. Depois envie:

```http
PUT /api/v1/admin/spaces/{stationId}/station-placement
```

```json
{
  "roomId": "UUID da sala cadastrada",
  "number": 1,
  "x": 25.50,
  "y": 40.00
}
```

A operação exige `ADMIN` com `MANAGE_SPACES`, sessão e CSRF. Retorna 200 com o
`Space` atualizado. O campo `stationPlacement` contém `roomId`, `number`, `x` e `y`;
fica ausente em salas, mesas e estações ainda sem posição. O GET administrativo de um
espaço também retorna esse campo. A edição básica de nome, capacidade e descrição
preserva a posição, mesmo quando o frontend não envia esse campo.

O número é um inteiro positivo, único dentro da sala, inclusive para estações
inativas. Salas diferentes podem repetir números. `x` e `y` são números de 0 a 100,
com até duas casas decimais, em porcentagem a partir da esquerda e do topo. Esse
sistema de coordenadas permite ao frontend adaptar o mapa ao tamanho da tela.
O desenho real, a imagem da planta e as posições serão informados pela equipe;
a migração não inventa a disposição física nem cadastra as 11/12 estações citadas
no levantamento. A capacidade da sala representa pessoas, não um limite automático
para a quantidade de estações cadastradas.

Repetir a posição é permitido. O mesmo PUT pode renumerar, reposicionar ou transferir
a estação para outra sala, mantendo UUID, nome, situação e data de criação. A edição
não cria nem apaga espaços. Não há exclusão física ou remoção de vínculo nesta API;
para retirar uma estação do uso público, desative-a pelo endpoint de situação.

Erros de formato, campos ausentes ou fora dos limites retornam 400. Cadastro
inexistente retorna 404. Número já usado, destino diferente de `ROOM` ou origem
diferente de `WORKSTATION` retornam 409, com `ProblemDetail` centralizado. Salas com
estações vinculadas e estações posicionadas não podem mudar para outro tipo. É
possível continuar editando os demais campos e ativar/desativar esses cadastros.

## Consultar o mapa

| Método | Endpoint | Acesso |
|---|---|---|
| GET | `/api/v1/admin/spaces/{roomId}/stations` | `ADMIN` com `MANAGE_SPACES`; inclui estações inativas |
| GET | `/api/v1/spaces/{roomId}/stations` | Público; somente estações ativas de sala ativa |

As respostas usam `SpacePage`, com `items`, `page`, `size`, `totalElements` e
`totalPages`. Os parâmetros são `page` a partir de 0 e `size` entre 1 e 100, padrão
20. A ordenação é por número e UUID, não por nome, para que a estação 2 venha antes
da 10. A interface deve carregar as páginas necessárias para desenhar a sala inteira.
Uma sala sem estações retorna uma página vazia. Sala inexistente, tipo diferente
de `ROOM` ou sala inativa na consulta pública retorna 404. Desativar uma sala oculta
suas estações nesse mapa público sem alterar a situação individual delas.
O catálogo geral `/api/v1/spaces` também oculta estações vinculadas a salas inativas.

O mapa informa a estrutura física e quais cadastros estão habilitados. `active`
não significa livre no horário escolhido. O editor administrativo já permite arrastar
as estações e salvar posições. A seleção pública usa a consulta por intervalo.

## Persistência

V6 acrescenta os campos à tabela `spaces`, deixando cadastros existentes sem posição.
As migrações anteriores permanecem preservadas. Restrições no PostgreSQL garantem
numeração única na sala, coordenadas válidas, preenchimento completo e vínculo
apenas entre estação e sala. `room_type` é uma coluna gerada que permite à chave
estrangeira validar também o tipo da sala; não é um novo campo editável da API.

Atualizações da posição bloqueiam sala e estação em ordem de UUID. A edição básica
e a mudança de situação também bloqueiam o espaço antes de alterá-lo. Isso preserva
os campos quando há escritas simultâneas e impede que duas estações assumam o mesmo
número ou que uma sala mude de tipo enquanto recebe uma estação.

O contrato e os tipos TypeScript gerados incluem essas operações. A configuração
de funcionamento continua independente, conforme [Funcionamento do Hub](19-funcionamento-do-hub.md).
