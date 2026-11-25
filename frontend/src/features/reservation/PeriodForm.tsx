import { zodResolver } from '@hookform/resolvers/zod'
import { useForm, useWatch } from 'react-hook-form'
import { z } from 'zod'
import { Button } from '../../shared/ui/Button.tsx'
import { FormCard } from '../../shared/ui/FormCard.tsx'
import { TextField } from '../../shared/ui/TextField.tsx'
import { DayAgenda } from './DayAgenda.tsx'
import { maskDate, maskTime, withMask } from './inputMasks.ts'
import {
  BRAZILIAN_DATE,
  isCalendarDate,
  TIME_24H,
  toBrazilianDate,
  toIsoDate,
  today,
} from './reservationDates.ts'
import type { ReservationPeriod } from './reservationDraft.ts'
import { RESERVE } from './reservationRoutes.ts'
import { StepActions } from './StepActions.tsx'

function time(required: string) {
  return z
    .string()
    .min(1, required)
    .regex(TIME_24H, 'Informe o horário no formato hh:mm, de 00:00 a 23:59.')
}

const schema = z
  .object({
    date: z
      .string()
      .min(1, 'Informe a data.')
      .regex(BRAZILIAN_DATE, 'Informe a data no formato dd/mm/aaaa.')
      .transform(toIsoDate)
      .refine(isCalendarDate, 'Informe uma data válida.')
      .refine((date) => date >= today(), 'Escolha uma data a partir de hoje.'),
    start: time('Informe o início.'),
    end: time('Informe o fim.'),
  })
  .refine(
    (period) =>
      !TIME_24H.test(period.start) ||
      !TIME_24H.test(period.end) ||
      period.end > period.start,
    { path: ['end'], message: 'O fim deve ser depois do início.' },
  )

type PeriodFormProps = {
  spaceId: string
  period?: ReservationPeriod
  onSubmit: (period: ReservationPeriod) => void
  onEdit: () => void
}

export function PeriodForm({
  spaceId,
  period,
  onSubmit,
  onEdit,
}: PeriodFormProps) {
  const { register, handleSubmit, formState, control, setValue, setFocus } =
    useForm({
      resolver: zodResolver(schema),
      defaultValues: period && {
        ...period,
        date: toBrazilianDate(period.date),
      },
    })

  const typedDate = useWatch({ control, name: 'date' })
  const date = typedDate ? toIsoDate(typedDate) : today()
  const showAgenda =
    (!typedDate || BRAZILIAN_DATE.test(typedDate)) &&
    isCalendarDate(date) &&
    date >= today()

  function changeDate(next: string) {
    setValue('date', toBrazilianDate(next), { shouldValidate: true })
    onEdit()
  }

  return (
    <FormCard onSubmit={handleSubmit(onSubmit)} onChange={onEdit}>
      <TextField
        label="Data"
        inputMode="numeric"
        autoComplete="off"
        placeholder="dd/mm/aaaa"
        maxLength={10}
        hint="Dia, mês e ano, por exemplo 10/03/2027."
        error={formState.errors.date?.message}
        {...withMask(register('date'), maskDate)}
      />
      {showAgenda && (
        <DayAgenda
          spaceId={spaceId}
          date={date}
          onDateChange={changeDate}
          onChoose={(selected) => {
            changeDate(selected.date)
            setValue('start', selected.start, { shouldValidate: true })
            setValue('end', selected.end, { shouldValidate: true })
            setFocus('start')
          }}
        />
      )}
      <TextField
        label="Início"
        inputMode="numeric"
        autoComplete="off"
        placeholder="hh:mm"
        maxLength={5}
        hint="Horário de 24 horas, por exemplo 14:30."
        error={formState.errors.start?.message}
        {...withMask(register('start'), maskTime)}
      />
      <TextField
        label="Fim"
        inputMode="numeric"
        autoComplete="off"
        placeholder="hh:mm"
        maxLength={5}
        hint="Informe o horário local do Hub."
        error={formState.errors.end?.message}
        {...withMask(register('end'), maskTime)}
      />
      <StepActions backTo={RESERVE} backLabel="Voltar aos espaços">
        <Button type="submit">Ver disponibilidade</Button>
      </StepActions>
    </FormCard>
  )
}
