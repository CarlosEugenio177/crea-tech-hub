import { zodResolver } from '@hookform/resolvers/zod'
import { useId, useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { Button } from '../../shared/ui/Button.tsx'
import { CheckboxField } from '../../shared/ui/CheckboxField.tsx'
import { FormCard } from '../../shared/ui/FormCard.tsx'
import { ProblemNotice } from '../../shared/ui/ProblemNotice.tsx'
import { TextField } from '../../shared/ui/TextField.tsx'
import { maskTime, withMask } from '../reservation/inputMasks.ts'
import { TIME_24H } from '../reservation/reservationDates.ts'
import styles from './OperatingHoursPage.module.css'
import type {
  OperatingHours,
  OperatingHoursRequest,
} from './operatingHoursApi.ts'

const schema = z
  .object({
    openingTime: z
      .string()
      .regex(TIME_24H, 'Informe um horário válido no formato hh:mm.'),
    closingTime: z
      .string()
      .regex(TIME_24H, 'Informe um horário válido no formato hh:mm.'),
  })
  .refine(
    (values) =>
      !TIME_24H.test(values.openingTime) ||
      !TIME_24H.test(values.closingTime) ||
      values.closingTime > values.openingTime,
    {
      path: ['closingTime'],
      message: 'O fechamento deve ser depois da abertura.',
    },
  )

type DayOption = {
  value: OperatingHoursRequest['daysOfWeek'][number]
  label: string
}

const DAYS: DayOption[] = [
  { value: 'MONDAY', label: 'Segunda-feira' },
  { value: 'TUESDAY', label: 'Terça-feira' },
  { value: 'WEDNESDAY', label: 'Quarta-feira' },
  { value: 'THURSDAY', label: 'Quinta-feira' },
  { value: 'FRIDAY', label: 'Sexta-feira' },
  { value: 'SATURDAY', label: 'Sábado' },
  { value: 'SUNDAY', label: 'Domingo' },
]

type OperatingHoursFormProps = {
  hours: OperatingHours
  error: unknown
  isPending: boolean
  onSubmit: (hours: OperatingHoursRequest) => void
}

export function OperatingHoursForm({
  hours,
  error,
  isPending,
  onSubmit,
}: OperatingHoursFormProps) {
  const [days, setDays] = useState(hours.daysOfWeek)
  const [daysError, setDaysError] = useState<string>()
  const daysErrorId = useId()
  const { register, handleSubmit, formState } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      openingTime: hours.openingTime,
      closingTime: hours.closingTime,
    },
  })

  const toggleDay = (day: OperatingHoursRequest['daysOfWeek'][number]) => {
    setDaysError(undefined)
    setDays((current) =>
      current.includes(day)
        ? current.filter((value) => value !== day)
        : [...current, day].sort(
            (left, right) =>
              DAYS.findIndex((item) => item.value === left) -
              DAYS.findIndex((item) => item.value === right),
          ),
    )
  }

  const submit = handleSubmit((values) => {
    if (days.length === 0) {
      setDaysError('Selecione pelo menos um dia de funcionamento.')
      return
    }
    onSubmit({ ...values, daysOfWeek: days })
  })

  return (
    <FormCard onSubmit={submit}>
      <ProblemNotice error={error} />
      <fieldset
        className={styles.days}
        aria-invalid={daysError ? true : undefined}
        aria-describedby={daysError ? daysErrorId : undefined}
      >
        <legend className={styles.legend}>Dias de funcionamento</legend>
        {DAYS.map((day) => (
          <CheckboxField
            key={day.value}
            label={day.label}
            checked={days.includes(day.value)}
            onChange={() => toggleDay(day.value)}
          />
        ))}
        {daysError && (
          <p id={daysErrorId} role="alert" className={styles.error}>
            {daysError}
          </p>
        )}
      </fieldset>
      <TextField
        label="Abertura"
        inputMode="numeric"
        autoComplete="off"
        placeholder="08:00"
        maxLength={5}
        hint="Horário local do Hub, de 00:00 a 23:59."
        error={formState.errors.openingTime?.message}
        {...withMask(register('openingTime'), maskTime)}
      />
      <TextField
        label="Fechamento"
        inputMode="numeric"
        autoComplete="off"
        placeholder="18:00"
        maxLength={5}
        hint="O fechamento deve ser depois da abertura."
        error={formState.errors.closingTime?.message}
        {...withMask(register('closingTime'), maskTime)}
      />
      <Button type="submit" disabled={isPending}>
        {isPending ? 'Salvando…' : 'Salvar horário'}
      </Button>
    </FormCard>
  )
}
