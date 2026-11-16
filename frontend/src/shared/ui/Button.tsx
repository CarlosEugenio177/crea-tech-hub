import type { ReactNode } from 'react'
import styles from './Button.module.css'

type ButtonProps = {
  type?: 'button' | 'submit'
  variant?: 'primary' | 'secondary'
  disabled?: boolean
  onClick?: () => void
  children: ReactNode
}

export function Button({
  type = 'button',
  variant = 'primary',
  disabled,
  onClick,
  children,
}: ButtonProps) {
  return (
    <button
      type={type}
      className={`${styles.button} ${styles[variant]}`}
      disabled={disabled}
      onClick={onClick}
    >
      {children}
    </button>
  )
}
