import { zodResolver } from '@hookform/resolvers/zod'
import { useMemo } from 'react'
import { useController, useForm, useWatch } from 'react-hook-form'
import { Button } from '../../shared/ui/Button.tsx'
import { FormCard } from '../../shared/ui/FormCard.tsx'
import { useDayAgenda, type AvailableStation } from './availabilityApi.ts'
import { DayAgenda } from './DayAgenda.tsx'
import {
  bookingWindows,
  endTimes,
  startTimes,
  stationsForPeriod,
} from './periodAvailability.ts'
import { PeriodFields } from './PeriodFields.tsx'
import styles from './PeriodForm.module.css'
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
  onSubmit: (period: ReservationPeriod, stations: AvailableStation[]) => void
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
    setError,
  } = useForm({
    resolver: zodResolver(periodSchema),
    defaultValues: {
      date: toBrazilianDate(period?.date ?? today()),
      start: period?.start ?? '',
      end: period?.end ?? '',
    },
  })

  const { field: start } = useController({ control, name: 'start' })
  const { field: end } = useController({ control, name: 'end' })
  const typedDate = useWatch({ control, name: 'date' })
  const date = toIsoDate(typedDate)
  const showAgenda =
    BRAZILIAN_DATE.test(typedDate) && isCalendarDate(date) && date >= today()
  const agenda = useDayAgenda(spaceId, showAgenda ? date : undefined)
  const data = agenda.isError ? undefined : agenda.data
  const windows = useMemo(() => bookingWindows(data), [data])
  const starts = useMemo(() => startTimes(windows), [windows])
  const ends = endTimes(windows, start.value)

  function clearPeriod() {
    setValue('start', '')
    setValue('end', '')
  }

  function selectDate(next: string) {
    if (next === date) return
    setValue('date', toBrazilianDate(next), { shouldValidate: true })
    clearPeriod()
    onEdit()
  }

  function submit(next: ReservationPeriod) {
    if (!data || next.date !== date) return
    if (!starts.includes(next.start) || !ends.includes(next.end)) {
      setError('end', { message: 'Escolha um período livre na agenda.' })
      return
    }
    onSubmit(next, stationsForPeriod(data, next.start, next.end))
  }

  return (
    <FormCard variant="plain" onSubmit={handleSubmit(submit)} onChange={onEdit}>
      <PeriodFields
        date={register('date', { onChange: clearPeriod })}
        start={{
          ...start,
          options: starts,
          onChange: (value) => {
            start.onChange(value)
            if (end.value && !endTimes(windows, value).includes(end.value)) {
              setValue('end', '')
            }
          },
        }}
        end={{ ...end, options: ends }}
        errors={errors}
      >
        {showAgenda && (
          <DayAgenda
            data={data}
            unavailable={agenda.isError}
            date={date}
            start={start.value}
            end={end.value}
            onDateChange={selectDate}
            onRetry={() => void agenda.refetch()}
            onChoose={(selected) => {
              setValue('start', selected.start, { shouldValidate: true })
              setValue('end', selected.end, { shouldValidate: true })
              onEdit()
              setFocus('start')
            }}
          />
        )}
      </PeriodFields>
      <div className={styles.actions}>
        <StepActions
          backTo={back?.path ?? RESERVE}
          backLabel={back?.label ?? 'Voltar aos espaços'}
        >
          <Button type="submit" disabled={showAgenda && starts.length === 0}>
            Continuar
          </Button>
        </StepActions>
      </div>
    </FormCard>
  )
}
