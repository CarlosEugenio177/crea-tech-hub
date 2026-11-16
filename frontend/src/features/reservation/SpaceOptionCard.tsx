import { Button } from '../../shared/ui/Button.tsx'
import styles from './SpaceOptionCard.module.css'
import type { SpaceOption } from './spaceOptions.ts'

type SpaceOptionCardProps = {
  option: SpaceOption
  onChoose: () => void
}

export function SpaceOptionCard({ option, onChoose }: SpaceOptionCardProps) {
  return (
    <li className={styles.card}>
      <img
        className={styles.illustration}
        src={option.illustration}
        alt={option.illustrationAlt}
      />
      <h2 className={styles.name}>{option.name}</h2>
      <p className={styles.description}>{option.description}</p>
      <p className={styles.tag}>{option.tag}</p>
      <Button onClick={onChoose}>Escolher {option.name}</Button>
    </li>
  )
}
