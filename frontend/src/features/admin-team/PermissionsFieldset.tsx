import type { UseFormRegisterReturn } from 'react-hook-form'
import { CheckboxField } from '../../shared/ui/CheckboxField.tsx'
import styles from './PermissionsFieldset.module.css'
import type { PermissionOption } from './teamApi.ts'

type PermissionsFieldsetProps = {
  options: PermissionOption[]
  registration: UseFormRegisterReturn
}

export function PermissionsFieldset({
  options,
  registration,
}: PermissionsFieldsetProps) {
  return (
    <fieldset className={styles.fieldset}>
      <legend className={styles.legend}>Permissões</legend>
      {options.map((option) => (
        <CheckboxField
          key={option.permission}
          label={option.description}
          value={option.permission}
          {...registration}
        />
      ))}
    </fieldset>
  )
}
