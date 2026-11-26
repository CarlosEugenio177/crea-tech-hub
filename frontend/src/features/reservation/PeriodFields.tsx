import type { ReactNode } from 'react'
import type { FieldErrors, UseFormRegisterReturn } from 'react-hook-form'
import { TextField } from '../../shared/ui/TextField.tsx'
import { TimeField, type TimeFieldControl } from '../../shared/ui/TimeField.tsx'
import { maskDate, withMask } from './inputMasks.ts'
import styles from './PeriodFields.module.css'
import type { ReservationPeriod } from './reservationDraft.ts'

type PeriodFieldsProps = {
  date: UseFormRegisterReturn<'date'>
  start: TimeFieldControl
  end: TimeFieldControl
  errors: FieldErrors<ReservationPeriod>
  children?: ReactNode
}

export function PeriodFields({
  date,
  start,
  end,
  errors,
  children,
}: PeriodFieldsProps) {
  return (
    <>
      {children}
      <TextField
        label="Data"
        inputMode="numeric"
        autoComplete="off"
        placeholder="dd/mm/aaaa"
        maxLength={10}
        error={errors.date?.message}
        {...withMask(date, maskDate)}
      />
      <div className={styles.times}>
        <TimeField label="Início" error={errors.start?.message} {...start} />
        <TimeField label="Fim" error={errors.end?.message} {...end} />
      </div>
    </>
  )
}
