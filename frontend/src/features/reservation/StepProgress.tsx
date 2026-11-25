import { Link } from 'react-router'
import { STEPS } from './steps.ts'
import styles from './StepProgress.module.css'

type StepProgressProps = { current: number }

export function StepProgress({ current }: StepProgressProps) {
  return (
    <ol className={styles.steps} aria-label="Etapas da solicitação">
      {STEPS.map((step, index) => {
        const number = index + 1
        const done = number < current

        return (
          <li
            key={step.to}
            aria-current={number === current ? 'step' : undefined}
          >
            {done ? (
              <Link to={step.to} className={styles.label}>
                {step.label}
              </Link>
            ) : (
              <span className={styles.label}>{step.label}</span>
            )}
          </li>
        )
      })}
    </ol>
  )
}
