import type { ReactNode } from 'react'
import type { FieldErrors, UseFormRegisterReturn } from 'react-hook-form'
import { TextField } from '../../shared/ui/TextField.tsx'
import { maskDate, maskTime, withMask } from './inputMasks.ts'
import type { ReservationPeriod } from './reservationDraft.ts'

type PeriodFieldsProps = {
  fields: Record<keyof ReservationPeriod, UseFormRegisterReturn>
  errors: FieldErrors<ReservationPeriod>
  children?: ReactNode
}

export function PeriodFields({ fields, errors, children }: PeriodFieldsProps) {
  return (
    <>
      <TextField
        label="Data"
        inputMode="numeric"
        autoComplete="off"
        placeholder="dd/mm/aaaa"
        maxLength={10}
        hint="Dia, mês e ano, por exemplo 10/03/2027."
        error={errors.date?.message}
        {...withMask(fields.date, maskDate)}
      />
      {children}
      <TextField
        label="Início"
        inputMode="numeric"
        autoComplete="off"
        placeholder="hh:mm"
        maxLength={5}
        hint="Horário de 24 horas, por exemplo 14:30."
        error={errors.start?.message}
        {...withMask(fields.start, maskTime)}
      />
      <TextField
        label="Fim"
        inputMode="numeric"
        autoComplete="off"
        placeholder="hh:mm"
        maxLength={5}
        hint="Informe o horário local do Hub."
        error={errors.end?.message}
        {...withMask(fields.end, maskTime)}
      />
    </>
  )
}
