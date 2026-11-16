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
            className={number > current ? styles.pending : styles.reached}
            aria-current={number === current ? 'step' : undefined}
          >
            <span className={styles.number}>{number}</span>
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
