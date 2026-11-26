import { zodResolver } from '@hookform/resolvers/zod'
import { useController, useForm } from 'react-hook-form'
import { z } from 'zod'
import { Button } from '../../shared/ui/Button.tsx'
import { FormCard } from '../../shared/ui/FormCard.tsx'
import { TextField } from '../../shared/ui/TextField.tsx'
import { PeriodFields } from './PeriodFields.tsx'
import { periodSchema } from './periodSchema.ts'
import { toBrazilianDate } from './reservationDates.ts'
import type { RecommendationCriteria } from './reservationDraft.ts'
import { RESERVE } from './reservationRoutes.ts'
import { StepActions } from './StepActions.tsx'

const schema = periodSchema.safeExtend({
  participants: z
    .string()
    .trim()
    .regex(/^[0-9]+$/, 'Informe o número de pessoas.')
    .transform(Number)
    .pipe(
      z
        .int()
        .min(1, 'Informe pelo menos uma pessoa.')
        .max(2147483647, 'Informe um número válido.'),
    ),
})

type RecommendationFormProps = {
  initial?: RecommendationCriteria
  onEdit: () => void
  onSubmit: (criteria: RecommendationCriteria) => void
}

export function RecommendationForm({
  initial,
  onEdit,
  onSubmit,
}: RecommendationFormProps) {
  const {
    register,
    control,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      participants: initial ? String(initial.participants) : '',
      date: initial ? toBrazilianDate(initial.period.date) : '',
      start: initial?.period.start ?? '',
      end: initial?.period.end ?? '',
    },
  })

  const { field: start } = useController({ control, name: 'start' })
  const { field: end } = useController({ control, name: 'end' })

  return (
    <FormCard
      variant="plain"
      onChange={onEdit}
      onSubmit={handleSubmit(({ participants, ...period }) =>
        onSubmit({ participants, period }),
      )}
    >
      <TextField
        label="Número de pessoas"
        inputMode="numeric"
        autoComplete="off"
        error={errors.participants?.message}
        {...register('participants')}
      />
      <PeriodFields
        date={register('date')}
        start={start}
        end={end}
        errors={errors}
      />
      <StepActions backTo={RESERVE} backLabel="Voltar aos espaços">
        <Button type="submit">Buscar espaços</Button>
      </StepActions>
    </FormCard>
  )
}
