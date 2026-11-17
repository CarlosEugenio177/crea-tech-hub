import { boundedPoint, type Point, type Space } from './layoutTypes.ts'

export type Dimensions = NonNullable<Space['roomDimensions']>
export type Guide = { axis: 'x' | 'y'; value: number; spacing?: number }
type Axis = 'x' | 'y'

export function snapPosition(
  position: Point,
  others: Point[],
  bounds: { width: number; height: number },
  dimensions: Dimensions,
  grid: number,
): { point: Point; guides: Guide[] } {
  const guides: Guide[] = []
  const snapAxis = (axis: Axis, pixels: number, meters: number) => {
    const threshold = (8 * 100) / pixels
    const step = (grid * 100) / meters
    const candidates: { value: number; spacing?: number }[] = [
      { value: Math.round(position[axis] / step) * step },
      ...others.map((point) => ({ value: point[axis] })),
    ]
    const crossAxis = axis === 'x' ? 'y' : 'x'
    const crossThreshold =
      (8 * 100) / (axis === 'x' ? bounds.height : bounds.width)
    const line = others
      .filter(
        (point) =>
          Math.abs(point[crossAxis] - position[crossAxis]) <= crossThreshold,
      )
      .map((point) => point[axis])
      .sort((a, b) => a - b)
    for (let i = 1; i < line.length; i++) {
      const start = line[i - 1]
      const end = line[i]
      if (start === undefined || end === undefined || end - start < 0.1)
        continue
      const spacing = end - start
      candidates.push(
        { value: start - spacing, spacing },
        { value: end + spacing, spacing },
        { value: (start + end) / 2, spacing: spacing / 2 },
      )
    }
    const closest = candidates
      .filter((candidate) => candidate.value >= 0 && candidate.value <= 100)
      .sort(
        (a, b) =>
          Math.abs(a.value - position[axis]) -
          Math.abs(b.value - position[axis]),
      )[0]
    if (!closest || Math.abs(closest.value - position[axis]) > threshold)
      return position[axis]
    guides.push({ axis, ...closest })
    return closest.value
  }
  const x = snapAxis('x', bounds.width, dimensions.width)
  const y = snapAxis('y', bounds.height, dimensions.length)
  return { point: boundedPoint(x, y), guides }
}

export function arrangePoints(
  points: Point[],
  axis: Axis,
  distribute: boolean,
): Point[] {
  const sorted = points
    .map((point, index) => ({ point, index }))
    .sort((a, b) => a.point[axis] - b.point[axis])
  const first = sorted[0]?.point[axis] ?? 0
  const last = sorted.at(-1)?.point[axis] ?? first
  const result = [...points]
  sorted.forEach(({ point, index }, i) => {
    const value =
      distribute && sorted.length > 1
        ? first + ((last - first) * i) / (sorted.length - 1)
        : (first + last) / 2
    result[index] = boundedPoint(
      axis === 'x' ? value : point.x,
      axis === 'y' ? value : point.y,
    )
  })
  return result
}
