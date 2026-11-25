# Funcionamento do Hub

Esta parte do CTH-15 permite manter os dias e o intervalo diário de funcionamento
sem alterar código. A configuração inicial é segunda a sexta, das 08:00 às 18:00.
Uma configuração única vale para o Hub; todos os dias selecionados usam o mesmo intervalo.

## Contrato

| Método | Endpoint | Acesso |
|---|---|---|
| GET | `/api/v1/hub/operating-hours` | Consulta pública, sem login |
| GET | `/api/v1/admin/hub/operating-hours` | `ADMIN` com `MANAGE_SPACES` |
| PUT | `/api/v1/admin/hub/operating-hours` | `ADMIN` com `MANAGE_SPACES` e CSRF |

O PUT substitui toda a configuração e retorna 200 com os valores persistidos:

```json
{
  "daysOfWeek": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
  "openingTime": "08:00",
  "closingTime": "18:00"
}
```

Os dias são os nomes de `DayOfWeek`, de `MONDAY` a `SUNDAY`. É necessário selecionar
pelo menos um dia. A resposta ordena os dias de segunda a domingo e não repete valores.
Os horários usam exatamente `HH:mm`, em formato de 24 horas e com precisão de minutos,
de `00:00` a `23:59`. São horários locais do Hub no Piauí, não instantes UTC.

O fechamento deve ser depois da abertura, no mesmo dia. Esta configuração não suporta
turnos que atravessem a meia-noite, múltiplos intervalos no mesmo dia ou horários
diferentes para cada dia da semana. Essas possibilidades exigem uma nova decisão de escopo.
Entradas inválidas retornam 400 com `ProblemDetail`; o intervalo invertido/igual gera
erro de campo `intervalValid`. Uma atualização inválida preserva os valores existentes.

A permissão `MANAGE_ACCESS` por si só não autoriza editar o funcionamento. Não há
endpoints para criar configurações adicionais ou apagar a configuração. Alterações
concorrentes são serializadas no PostgreSQL, evitando misturar dias e horários de
operações diferentes. A última atualização concluída prevalece.

## Persistência e integração

A migração V5 cria `hub_operating_hours` e `hub_operating_days`, inicializa o padrão e
preserva contas, permissões e espaços existentes. O banco restringe os dias válidos,
impede duplicidades e exige intervalo crescente com precisão de minutos.

O contrato oficial `openapi/admin-api.json` e os tipos derivados incluem os três
endpoints. A interface pode consultar o GET público para exibir as informações atuais;
o horário de funcionamento não exige autenticação para ser conhecido.

O painel administrativo permite editar a configuração. O formulário público exibe
os dias e horários. A disponibilidade e o envio dos pedidos usam a configuração para
validar o intervalo escolhido. A configuração não cria slots fixos nem altera
reservas existentes.
