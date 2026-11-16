import styles from './StatusBadge.module.css'

type StatusBadgeProps = { active: boolean }

export function StatusBadge({ active }: StatusBadgeProps) {
  return (
    <span className={active ? styles.active : styles.inactive}>
      {active ? 'Ativo' : 'Inativo'}
    </span>
  )
}
