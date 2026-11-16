import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { Button } from '../../shared/ui/Button.tsx'
import { FormCard } from '../../shared/ui/FormCard.tsx'
import { TextField } from '../../shared/ui/TextField.tsx'
import { today } from './reservationDates.ts'
import type { ReservationPeriod } from './reservationDraft.ts'
import { RESERVE } from './reservationRoutes.ts'
import { StepActions } from './StepActions.tsx'

const schema = z
  .object({
    date: z.string().min(1, 'Informe a data.'),
    start: z.string().min(1, 'Informe o início.'),
    end: z.string().min(1, 'Informe o fim.'),
  })
  .refine((period) => period.date === '' || period.date >= today(), {
    path: ['date'],
    message: 'Escolha uma data a partir de hoje.',
  })
  .refine(
    (period) =>
      period.start === '' || period.end === '' || period.end > period.start,
    { path: ['end'], message: 'O fim deve ser depois do início.' },
  )

type PeriodFormProps = {
  period?: ReservationPeriod
  onSubmit: (period: ReservationPeriod) => void
}

export function PeriodForm({ period, onSubmit }: PeriodFormProps) {
  const { register, handleSubmit, formState } = useForm({
    resolver: zodResolver(schema),
    defaultValues: period,
  })

  return (
    <FormCard onSubmit={handleSubmit(onSubmit)}>
      <TextField
        label="Data"
        type="date"
        min={today()}
        error={formState.errors.date?.message}
        {...register('date')}
      />
      <TextField
        label="Início"
        type="time"
        error={formState.errors.start?.message}
        {...register('start')}
      />
      <TextField
        label="Fim"
        type="time"
        hint="Funcionamento padrão: segunda a sexta, das 8h às 18h. A equipe confere a disponibilidade."
        error={formState.errors.end?.message}
        {...register('end')}
      />
      <StepActions backTo={RESERVE} backLabel="Voltar aos espaços">
        <Button type="submit">Continuar</Button>
      </StepActions>
    </FormCard>
  )
}
