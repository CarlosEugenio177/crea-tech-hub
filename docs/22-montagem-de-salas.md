# Modalidade de reserva e montagem de salas

Administradores com `MANAGE_SPACES` podem montar o mapa de uma sala em
`/admin/espacos/{id}/mapa`, pelo botão **Montar sala** na lista de espaços.

## Uso do editor

1. Cadastre a sala com nome e capacidade no cadastro de espaços.
2. Em **Montar sala**, escolha **Espaço inteiro** ou **Por estação** e salve a modalidade.
3. Ajuste a largura e o comprimento do mapa em metros e clique em **Aplicar medidas**.
   Os valores iniciais de 10 × 6 m são uma referência editável, não uma medição do Hub.
4. Para reservar por estação, informe a quantidade e quantas ficam por fileira.
   Por exemplo, 12 estações com 4 por fileira geram três fileiras numeradas.
5. Arraste as estações para ajustar a disposição e clique em **Salvar mapa**.

**Auxiliar alinhamento** encaixa o arraste na grade de 25 cm, 50 cm ou 1 m e nas
linhas das outras estações. Guias aparecem perto do alinhamento, inclusive ao
repetir o espaçamento de uma fileira. Alt suspende o encaixe durante o arraste.
As distâncias usam os centros das estações; os ícones não representam as medidas
dos móveis nem validam corredores. As setas e os botões permitem o ajuste fino.

Use Ctrl/⌘ com clique, **Selecionar por número** ou **Selecionar todas** para atuar
em várias estações. É possível alinhar em fileira/coluna e igualar as distâncias
horizontais/verticais. A distribuição mantém os extremos da seleção.

**Remover do mapa** ou Delete retira as selecionadas do rascunho. **Desfazer remoções**
as restaura antes da gravação. Ao salvar, as estações removidas são desvinculadas
e desativadas; os registros e seus UUIDs são preservados. Nenhuma outra estação é
renumerada. Retirar todas permite mudar a modalidade para espaço inteiro.

O arraste funciona com mouse e toque. Também é possível selecionar uma estação e
usar as setas do teclado ou os botões de direção. Shift com as setas aumenta o passo.
As posições permanecem dentro do mapa e são armazenadas em porcentagem, permitindo
que o desenho se adapte ao tamanho da tela. O editor usa
[Pointer Events](https://developer.mozilla.org/en-US/docs/Web/API/Pointer_events),
sem uma biblioteca de desenho ou um motor 3D.

Arrastar, aplicar medidas, alinhar e remover alteram um rascunho local. **Salvar mapa**
persiste medidas, posições alteradas e remoções em uma transação; **Descartar** retorna
ao mapa do servidor. Uma falha
ao atualizar os dados em segundo plano preserva o rascunho. Nesse caso, recarregue o
mapa antes de continuar. Em conflito com outra edição, as posições locais permanecem
visíveis e há uma ação explícita para descartar e recarregar.

**Adicionar estações** cria novos registros com números depois do maior número ainda
vinculado à sala. Estações mantidas, inclusive inativas, preservam UUID, número e posição.
A geração considera as células ocupadas na grade inicial; o ajuste fino fica no
editor. Não há exclusão física ou recriação das estações ao salvar posições.

Ativar/desativar uma estação usa a API de situação existente. Salve ou descarte
posições pendentes antes de mudar a situação. **Ativa/Inativa** se refere ao cadastro,
não à disponibilidade em uma data. As reservas e suas cores no mapa público continuam
pendentes da implementação da disponibilidade.

## Modalidades

| Valor | Uso |
|---|---|
| `WHOLE_SPACE` | Reserva do espaço inteiro; padrão de um novo cadastro |
| `PER_STATION` | Ambiente com estações reservadas individualmente; permitido somente em `ROOM` |

Coworking e sala de estudos são ambientes `ROOM` com modalidade `PER_STATION`.
Uma estação continua sendo um espaço `WORKSTATION`, com capacidade 1 e modalidade
`WHOLE_SPACE` para representar a unidade individual.

Estações só podem ser vinculadas a ambientes `PER_STATION`. Um ambiente com estações
vinculadas, inclusive inativas, não pode mudar para `WHOLE_SPACE`; isso preserva os
vínculos e o histórico. O tipo do ambiente e a quantidade de pessoas são independentes
da quantidade de estações. Não há exigência de que a capacidade seja igual à contagem.

## Contrato

Todas as operações abaixo exigem sessão, `MANAGE_SPACES` e CSRF.

| Método | Endpoint | Corpo | Resposta |
|---|---|---|---|
| PUT | `/api/v1/admin/spaces/{id}/booking-mode` | `{"bookingMode":"PER_STATION"}` | 200 com `Space` |
| POST | `/api/v1/admin/spaces/{roomId}/stations/batch` | `{"count":12,"columns":4}` | 201 com as estações criadas |
| PUT | `/api/v1/admin/spaces/{roomId}/station-positions` | Posições alteradas, como abaixo | 200 com as estações atualizadas |
| PUT | `/api/v1/admin/spaces/{roomId}/layout` | Versão da sala, medidas, posições e remoções | 200 com a sala atualizada |

```json
{
  "positions": [
    { "id": "UUID da estação", "version": 1, "x": 25.50, "y": 40.00 }
  ]
}
```

`count` e `columns` são inteiros de 1 a 100. Esse é um limite por operação, não um
limite de capacidade da sala. Uma gravação de posições aceita de 1 a 1.000 alterações,
sem repetir estações. `x` e `y` vão de 0 a 100 com até duas casas decimais.

Cada estação envia a `version` recebida do servidor. Se outra operação tiver alterado
o registro, a gravação inteira retorna 409 sem salvar parcialmente as posições. Sala
em modalidade incompatível ou estação pertencente a outra sala também retorna 409;
cadastro inexistente retorna 404; campos inválidos retornam 400 com `ProblemDetail`.

O editor usa `/layout` para salvar o conjunto sem mudanças parciais:

```json
{
  "version": 1,
  "width": 8.5,
  "length": 5,
  "positions": [{ "id": "UUID mantido", "version": 2, "x": 25, "y": 50 }],
  "removedStations": [{ "id": "UUID removido", "version": 1 }]
}
```

As duas listas são obrigatórias e podem estar vazias, com até 1.000 itens cada.
Uma estação não pode aparecer em ambas. Medidas vão de 1 a 100 metros, com até
duas casas decimais. A versão da sala e as versões das estações alteradas/removidas
são verificadas antes de qualquer gravação. Uma remoção concorrente com um movimento
da mesma estação só permite concluir uma das operações. A API anterior de posições
permanece disponível. `Space` e `PublicSpace` incluem `roomDimensions` nas salas.

O GET de estações da sala continua paginado. O editor carrega todas as páginas antes
de habilitar a edição, incluindo estações inativas. O cadastro e o catálogo público
agora retornam `bookingMode`; a resposta administrativa `Space` inclui `version`.
Nome, tipo, capacidade e descrição continuam usando o corpo de cadastro anterior:
editar esses campos preserva a modalidade e a posição.

## Persistência

V8 adiciona `booking_mode` e a versão otimista aos espaços. Salas que já possuem
estações passam a `PER_STATION`; os demais cadastros ficam em `WHOLE_SPACE`. UUIDs,
numeração, posições, situação, bloqueios e contas são preservados.

A chave estrangeira da estação verifica também a modalidade da sala. A geração em
lote bloqueia a sala durante a transação para serializar a numeração. O salvamento
de posições bloqueia os registros em ordem de UUID e valida todas as versões antes
de aplicar mudanças. O contrato OpenAPI e os tipos gerados acompanham estas operações.

V9 adiciona as dimensões do mapa às salas, sem alterar posições existentes. Elas
começam com a referência editável 10 × 6 m. A restrição do banco impede medidas
parciais, fora dos limites ou vinculadas a tipos diferentes de sala.
