import { useState, type SubmitEvent } from 'react'
import { Button } from '../../shared/ui/Button.tsx'
import { SelectField } from '../../shared/ui/SelectField.tsx'
import { TextField } from '../../shared/ui/TextField.tsx'
import {
  statusLabels,
  type ReservationFilters,
} from './adminReservationsApi.ts'
import styles from './AdminReservationPage.module.css'
import { maskDate } from './inputMasks.ts'
import {
  BRAZILIAN_DATE,
  isCalendarDate,
  toIsoDate,
} from './reservationDates.ts'

type Props = { onApply: (filters: ReservationFilters) => void }

export function AdminReservationFilters({ onApply }: Props) {
  const [date, setDate] = useState('')
  const [dateError, setDateError] = useState<string>()

  function submit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    const values = new FormData(event.currentTarget)
    const isoDate = date ? toIsoDate(date) : undefined
    if (isoDate && (!BRAZILIAN_DATE.test(date) || !isCalendarDate(isoDate))) {
      setDateError('Informe uma data válida no formato dd/mm/aaaa.')
      return
    }
    const status = Object.keys(statusLabels).find(
      (value): value is keyof typeof statusLabels =>
        value === values.get('status'),
    )
    const optional = (name: string) =>
      String(values.get(name) ?? '').trim() || undefined
    onApply({
      search: optional('search'),
      space: optional('space'),
      date: isoDate,
      status,
    })
  }

  return (
    <form
      className={styles.filters}
      onSubmit={submit}
      onReset={() => {
        setDate('')
        setDateError(undefined)
        onApply({})
      }}
    >
      <TextField label="Nome ou protocolo" name="search" maxLength={150} />
      <TextField label="Ambiente" name="space" maxLength={150} />
      <TextField
        label="Data da reserva"
        value={date}
        inputMode="numeric"
        placeholder="dd/mm/aaaa"
        error={dateError}
        onChange={(event) => {
          setDate(maskDate(event.target.value))
          setDateError(undefined)
        }}
      />
      <SelectField label="Situação" name="status">
        <option value="">Todas</option>
        {Object.entries(statusLabels).map(([value, label]) => (
          <option key={value} value={value}>
            {label}
          </option>
        ))}
      </SelectField>
      <div className={styles.actions}>
        <Button type="submit">Filtrar</Button>
        <button type="reset" className={styles.clear}>
          Limpar filtros
        </button>
      </div>
    </form>
  )
}
