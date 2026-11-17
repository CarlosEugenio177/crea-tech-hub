# Bloqueios pontuais de horário

Esta parte do CTH-16 permite ao administrador definir quando uma sala, mesa ou
estação não deve ser usada. As operações exigem sessão de `ADMIN` com
`MANAGE_SPACES`; criar e cancelar também exigem CSRF. `MANAGE_ACCESS` sozinho não
concede acesso aos bloqueios.

## Criar e cancelar

```http
POST /api/v1/admin/space-blocks
```

```json
{
  "spaceId": "UUID do espaço cadastrado",
  "startsAt": "2027-03-10T09:15",
  "endsAt": "2027-03-10T12:30",
  "reason": "Manutenção"
}
```

A criação retorna 201, o registro e `Location`. O motivo é obrigatório, tem até
500 caracteres e perde espaços nas extremidades. O backend identifica quem criou
o bloqueio pela sessão; o cliente não informa o administrador.

As datas e horas são locais do Hub no Piauí, no formato `aaaa-MM-ddTHH:mm`, sem
segundos ou deslocamento de fuso. Datas inexistentes, ano zero e fim igual ou anterior
ao início retornam 400 com `ProblemDetail`. O intervalo é `[startsAt, endsAt)`;
um bloqueio terminado às 10:00 não intersecta uma consulta iniciada às 10:00.

O administrador pode bloquear espaços inativos, horários fora do funcionamento do
Hub e intervalos que atravessem dias. Não há duração máxima ou exigência de data
futura nesta etapa. Bloqueios sobrepostos podem coexistir: cada um mantém seu motivo
e responsável. Cancelar um não cancela os demais.

Para cancelar, envie `POST /api/v1/admin/space-blocks/{id}/cancel`, sem corpo.
A resposta 200 mantém o registro original e acrescenta `cancelledBy` e `cancelledAt`.
Repetir a operação retorna o mesmo cancelamento, preservando o primeiro responsável
e horário, inclusive em requisições simultâneas. Não há exclusão física, reativação
ou edição do intervalo nesta API; corrigir um bloqueio exige cancelar e criar outro.

## Consultar

| Método | Endpoint | Resultado |
|---|---|---|
| GET | `/api/v1/admin/space-blocks` | Consulta paginada de bloqueios |
| GET | `/api/v1/admin/space-blocks/{id}` | Registro individual, inclusive cancelado |

Filtros opcionais:

- `spaceId`: considera bloqueios do espaço e, se for uma estação vinculada, os da sala.
- `active`: `true` traz os não cancelados; `false` traz os cancelados; omitido traz ambos.
- `from` e `to`: intervalo da consulta no mesmo formato das datas de criação. Podem
  ser usados separadamente. Se ambos existirem, `to` deve ser posterior a `from`.
- `page`: a partir de 0, padrão 0; `size`: de 1 a 100, padrão 20.

Exemplo:

```text
GET /api/v1/admin/space-blocks?spaceId=UUID&active=true&from=2027-03-10T09:00&to=2027-03-10T18:00
```

A consulta retorna registros que intersectam o intervalo, mesmo que tenham começado
antes de `from`. Sem datas, inclui todo o histórico correspondente aos outros filtros.
`active` indica ausência de cancelamento, não que o bloqueio esteja ocorrendo agora.

A resposta contém `items`, `page`, `size`, `totalElements` e `totalPages`, ordenados
por início e UUID. UUID válido sem cadastro retorna 404; entradas e paginação
inválidas retornam 400. Motivos e responsáveis são exclusivos da API administrativa.

## Salas e estações

O bloqueio da sala é aplicado às estações pelo vínculo atual. A consulta de uma
estação retorna seus próprios bloqueios e os da sala; `spaceId` na resposta continua
sendo o alvo original. Não são criadas cópias para cada estação. Bloquear uma estação
não bloqueia suas vizinhas nem cria um bloqueio na sala.

## Persistência e próximas etapas

A migração V7 cria `space_blocks` e preserva contas, espaços, posições e funcionamento.
O PostgreSQL protege o intervalo crescente, a precisão de minutos, o motivo e a
identificação de quem cancelou. O cancelamento bloqueia a linha durante a transação.
Datas de criação e cancelamento são instantes UTC; início e fim são horários locais.

O contrato OpenAPI e os tipos gerados incluem estas operações. A tela administrativa,
os bloqueios recorrentes e a consulta pública de disponibilidade serão entregues
depois. A aplicação desses bloqueios no envio de reservas e o aviso sobre reservas
já existentes dependem da implementação das reservas. Esta API não cria reservas,
não altera o funcionamento do Hub e não atribui disponibilidade aos itens do mapa.
