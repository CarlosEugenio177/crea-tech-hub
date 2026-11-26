import { zodResolver } from '@hookform/resolvers/zod'
import { useController, useForm, useWatch } from 'react-hook-form'
import { Button } from '../../shared/ui/Button.tsx'
import { FormCard } from '../../shared/ui/FormCard.tsx'
import { DayAgenda } from './DayAgenda.tsx'
import { PeriodFields } from './PeriodFields.tsx'
import { periodSchema } from './periodSchema.ts'
import {
  BRAZILIAN_DATE,
  isCalendarDate,
  toBrazilianDate,
  toIsoDate,
  today,
} from './reservationDates.ts'
import type { ReservationPeriod } from './reservationDraft.ts'
import { RESERVE } from './reservationRoutes.ts'
import { StepActions } from './StepActions.tsx'

type PeriodFormProps = {
  spaceId: string
  period?: ReservationPeriod
  onSubmit: (period: ReservationPeriod) => void
  onEdit: () => void
  back?: { path: string; label: string }
}

export function PeriodForm({
  spaceId,
  period,
  onSubmit,
  onEdit,
  back,
}: PeriodFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
    control,
    setValue,
    setFocus,
  } = useForm({
    resolver: zodResolver(periodSchema),
    defaultValues: period && { ...period, date: toBrazilianDate(period.date) },
  })

  const { field: start } = useController({ control, name: 'start' })
  const { field: end } = useController({ control, name: 'end' })
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
    <FormCard
      variant="plain"
      onSubmit={handleSubmit(onSubmit)}
      onChange={onEdit}
    >
      <PeriodFields
        date={register('date')}
        start={start}
        end={end}
        errors={errors}
      >
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
      </PeriodFields>
      <StepActions
        backTo={back?.path ?? RESERVE}
        backLabel={back?.label ?? 'Voltar aos espaços'}
      >
        <Button type="submit">Ver disponibilidade</Button>
      </StepActions>
    </FormCard>
  )
}
