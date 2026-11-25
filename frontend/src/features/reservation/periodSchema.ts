import { z } from 'zod'
import {
  BRAZILIAN_DATE,
  isCalendarDate,
  TIME_24H,
  today,
  toIsoDate,
} from './reservationDates.ts'

function time(required: string) {
  return z
    .string()
    .min(1, required)
    .regex(TIME_24H, 'Informe o horário no formato hh:mm, de 00:00 a 23:59.')
}

export const periodSchema = z
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
