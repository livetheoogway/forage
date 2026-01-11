# Score Functions API Reference

Score functions modify document scores in Function Score queries.

## Import

```java
import com.livetheoogway.forage.models.query.search.score.*;
```

## ConstantScoreFunction

Assigns a fixed score to all matching documents.

```java
new ConstantScoreFunction(float value)
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `value` | float | The constant score value |

**Scoring Mode**: Direct Value
**Formula**: `score = value`

**Example**:
```java
QueryBuilder.functionScoreQuery()
    .baseQuery(QueryBuilder.matchQuery("category", "featured").build())
    .constantScore(10.0f)  // All matches get score = 10.0
    .buildForageQuery()
```

## WeightedScoreFunction

Multiplies the base query score by a weight factor.

```java
new WeightedScoreFunction(float weight)
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `weight` | float | Multiplier for base score |

**Scoring Mode**: Multiplicative
**Formula**: `score = base_score × weight`

**Example**:
```java
QueryBuilder.functionScoreQuery()
    .baseQuery(QueryBuilder.matchQuery("title", "java").build())
    .scoreFunction(new WeightedScoreFunction(2.0f))  // Score × 2
    .buildForageQuery()
```

## FieldValueFactorFunction

Uses a numeric field's value as the score.

```java
new FieldValueFactorFunction(String field)
new FieldValueFactorFunction(String field, float factor)
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `field` | String | Numeric field name |
| `factor` | float | Optional multiplier (default: 1.0) |

**Scoring Mode**: Direct Value
**Formula**: `score = field_value × factor`

**Example**:
```java
// Score = rating
new FieldValueFactorFunction("rating")

// Score = rating × 1.5
new FieldValueFactorFunction("rating", 1.5f)
```

**Usage**:
```java
QueryBuilder.functionScoreQuery()
    .baseQuery(QueryBuilder.matchAllQuery().build())
    .fieldValueFactor("rating", 1.2f)
    .buildForageQuery(20)
```

## ScriptScoreFunction

Executes a JavaScript expression for custom scoring.

```java
new ScriptScoreFunction(String expression)
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `expression` | String | JavaScript expression |

**Scoring Mode**: Multiplicative
**Formula**: `score = base_score × expression_result`

**Available Variables**:

| Variable | Description |
|----------|-------------|
| `score` | Base query relevance score |
| `<fieldName>` | Any numeric DocValues field |

**Supported Operations**:
- Arithmetic: `+`, `-`, `*`, `/`
- Math functions: `ln()`, `log()`, `sqrt()`, `pow()`, `abs()`, `min()`, `max()`
- Trigonometry: `sin()`, `cos()`, `tan()`

**Examples**:
```java
// Blend relevance with rating
new ScriptScoreFunction("score * rating")

// Weighted combination
new ScriptScoreFunction("score * 0.7 + rating * 0.3")

// Logarithmic popularity boost
new ScriptScoreFunction("score * (1 + ln(popularity + 1))")

// Multiple fields
new ScriptScoreFunction("score * rating * freshness")
```

**Usage**:
```java
QueryBuilder.functionScoreQuery()
    .baseQuery(QueryBuilder.matchQuery("title", "java").build())
    .scoreFunction(new ScriptScoreFunction("score * rating"))
    .buildForageQuery()
```

## RandomScoreFunction

Generates deterministic pseudo-random scores.

```java
new RandomScoreFunction(long seed, String field)
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `seed` | long | Random seed (same seed = same order) |
| `field` | String | Numeric field for randomization |

**Scoring Mode**: Direct Value
**Formula**: `score = abs(sin(field_value + seed))`

**Example**:
```java
// Consistent random order per session
long sessionSeed = session.getId().hashCode();
new RandomScoreFunction(sessionSeed, "id_numeric")
```

**Usage**:
```java
QueryBuilder.functionScoreQuery()
    .baseQuery(QueryBuilder.matchAllQuery().build())
    .scoreFunction(new RandomScoreFunction(42L, "id_numeric"))
    .buildForageQuery(10)
```

## DecayFunction

Scores decrease based on distance from an origin value.

```java
new DecayFunction(
    double origin,
    double scale,
    double offset,
    double decay,
    DecayType decayType,
    String field
)
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `origin` | double | Optimal value (score = 1.0) |
| `scale` | double | Distance at which score = decay |
| `offset` | double | No decay within this distance |
| `decay` | double | Score at scale distance (0-1) |
| `decayType` | DecayType | GAUSS, EXP, or LINEAR |
| `field` | String | Numeric field name |

**Scoring Mode**: Direct Value

### DecayType Enum

```java
public enum DecayType {
    GAUSS,   // Gaussian bell curve
    EXP,     // Exponential decay
    LINEAR   // Linear decrease
}
```

### Decay Formulas

| Type | Formula |
|------|---------|
| GAUSS | `exp(ln(decay) × (distance/scale)²)` |
| EXP | `exp(ln(decay) × distance/scale)` |
| LINEAR | `max(0, (scale - distance) / (scale / (1 - decay)))` |

Where `distance = max(0, abs(field_value - origin) - offset)`

**Examples**:
```java
// Prefer books around 300 pages
new DecayFunction(
    300.0,          // origin: ideal page count
    100.0,          // scale: score = 0.5 at 100 pages away
    50.0,           // offset: no decay within 50 pages
    0.5,            // decay: score at scale distance
    DecayType.GAUSS,
    "numPages"
)

// Prefer recent items (epoch seconds)
new DecayFunction(
    System.currentTimeMillis() / 1000.0,  // origin: now
    86400 * 7,                             // scale: 7 days
    86400,                                 // offset: 1 day grace
    0.5,
    DecayType.EXP,
    "publishedAt"
)
```

**Usage**:
```java
QueryBuilder.functionScoreQuery()
    .baseQuery(QueryBuilder.matchQuery("category", "books").build())
    .scoreFunction(new DecayFunction(300.0, 100.0, 0.0, 0.5, DecayType.GAUSS, "pages"))
    .buildForageQuery()
```

## Scoring Mode Summary

| Function | Mode | Score Calculation |
|----------|------|-------------------|
| `ConstantScoreFunction` | Direct | `constant_value` |
| `WeightedScoreFunction` | Multiplicative | `base_score × weight` |
| `FieldValueFactorFunction` | Direct | `field_value × factor` |
| `ScriptScoreFunction` | Multiplicative | `base_score × expression` |
| `RandomScoreFunction` | Direct | `random(seed, field)` |
| `DecayFunction` | Direct | `decay(distance)` |

## Field Requirements

All score functions that use fields require numeric DocValues:

```java
// In document creation
new ForageDocument(item.getId(), item, Arrays.asList(
    new FloatField("rating", new float[]{item.getRating()}),
    new IntField("popularity", new int[]{item.getPopularity()}),
    new IntField("id_numeric", new int[]{item.getId().hashCode()})
));
```
