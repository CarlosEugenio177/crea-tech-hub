import { screen } from '@testing-library/react'
import type userEvent from '@testing-library/user-event'

export function timeControls(label: string) {
  return {
    hour: screen.getByRole('combobox', { name: `${label} — hora` }),
    minute: screen.getByRole('combobox', { name: `${label} — minutos` }),
  }
}

export async function selectTime(
  user: ReturnType<typeof userEvent.setup>,
  label: string,
  value: string,
) {
  const [hour, minute] = value.split(':')
  const controls = timeControls(label)
  await user.selectOptions(controls.hour, hour)
  await user.selectOptions(controls.minute, minute)
}
