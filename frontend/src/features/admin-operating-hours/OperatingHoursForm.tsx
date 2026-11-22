import { zodResolver } from '@hookform/resolvers/zod'
import { useId, useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { Button } from '../../shared/ui/Button.tsx'
import { CheckboxField } from '../../shared/ui/CheckboxField.tsx'
import { FormCard } from '../../shared/ui/FormCard.tsx'
import { ProblemNotice } from '../../shared/ui/ProblemNotice.tsx'
import { TextField } from '../../shared/ui/TextField.tsx'
import styles from './OperatingHoursPage.module.css'
import type {
  OperatingHours,
  OperatingHoursRequest,
} from './operatingHoursApi.ts'

const timePattern = /^(?:[01][0-9]|2[0-3]):[0-5][0-9]$/

const schema = z
  .object({
    openingTime: z
      .string()
      .regex(timePattern, 'Informe um horário válido no formato hh:mm.')
      .transform((value) => value),
    closingTime: z
      .string()
      .regex(timePattern, 'Informe um horário válido no formato hh:mm.')
      .transform((value) => value),
  })
  .refine((hours) => hours.closingTime > hours.openingTime, {
    path: ['closingTime'],
    message: 'O fechamento deve ser depois da abertura.',
  })

const DAYS: Array<{
  value: OperatingHoursRequest['daysOfWeek'][number]
  label: string
}> = [
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
        placeholder="08:00"
        hint="Horário local do Hub, de 00:00 a 23:59."
        error={formState.errors.openingTime?.message}
        {...register('openingTime')}
      />
      <TextField
        label="Fechamento"
        placeholder="18:00"
        hint="O fechamento deve ser depois da abertura."
        error={formState.errors.closingTime?.message}
        {...register('closingTime')}
      />
      <Button type="submit" disabled={isPending}>
        {isPending ? 'Salvando…' : 'Salvar horário'}
      </Button>
    </FormCard>
  )
}
