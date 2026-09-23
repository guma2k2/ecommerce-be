# Integration Test Plan — Create & Update Product API

## Overview
Tests cover `POST /products` (createProduct) and `PUT /products/{id}` (updateProduct).
Each case documents the input combination, expected DB state, and expected response shape.

---

## 1. CREATE PRODUCT — `POST /products`

### 1.1 Validation / Error Cases

| # | Case | Expected Error |
|---|------|----------------|
| V1 | `name` already exists in DB | `PRODUCT_NAME_ALREADY_EXISTS` |
| V2 | `slug` already exists in DB | `PRODUCT_SLUG_ALREADY_EXISTS` |
| V3 | `categoryId` provided but not found in DB | `CATEGORY_NOT_FOUND` |
| V4 | `brandId` provided but not found in DB | `BRAND_NOT_FOUND` |
| V5 | `attributes[].productAttributeId` not found in DB | `PRODUCT_ATTRIBUTE_NOT_FOUND` |
| V6 | `options[].productOptionId` not found in DB | `PRODUCT_OPTION_NOT_FOUND` |
| V7 | `variants[].attributeValues[].productAttributeId` not found in DB | `PRODUCT_ATTRIBUTE_NOT_FOUND` |
| V8 | Required fields missing (`name`, `slug`, `price`, etc.) | 400 Bean Validation |

---

### 1.2 Base Product Fields

| # | Case | Expected |
|---|------|----------|
| B1 | `categoryId = null` | Product saved with `category = null` |
| B2 | `categoryId` = valid ID | Product saved with resolved `Category` entity |
| B3 | `brandId = null` | Product saved with `brand = null` |
| B4 | `brandId` = valid ID | Product saved with resolved `Brand` entity |
| B5 | Both `categoryId` and `brandId` provided | Both resolved and saved correctly |

---

### 1.3 Media

| # | Case | Expected |
|---|------|----------|
| M1 | `medias = null` or `[]` | No `ProductMedia` rows inserted |
| M2 | 1 media provided | 1 `ProductMedia` row, mediaId resolved to URL |
| M3 | N medias provided | N `ProductMedia` rows, all media URLs returned |

---

### 1.4 Options (ProductOptionCombination + ProductOptionValue)

| # | Case | Expected |
|---|------|----------|
| O1 | `options = null` | No combinations/values saved; `savedOptionValues` empty |
| O2 | `options = []` | No combinations/values saved |
| O3 | 1 option, 0 values | 1 combination saved, 0 option values |
| O4 | 1 option, N values | 1 combination, N option values, all linked to combination |
| O5 | N options, each with M values | N combinations, N×M option values (e.g., Size×3, Color×2) |

---

### 1.5 Attributes (ProductAttributeValue)

| # | Case | Expected |
|---|------|----------|
| A1 | `attributes = null` | No `ProductAttributeValue` rows inserted |
| A2 | `attributes = []` | No `ProductAttributeValue` rows inserted |
| A3 | 1 attribute provided | 1 `ProductAttributeValue` row inserted |
| A4 | N attributes provided | N `ProductAttributeValue` rows inserted |

---

### 1.6 Variants (ProductVariant + VariantOptionValue + ProductVariantAttributeValue)

| # | Case | Expected |
|---|------|----------|
| VR1 | `variants = null` | No `ProductVariant` rows inserted |
| VR2 | `variants = []` | No `ProductVariant` rows inserted |
| VR3 | 1 variant, no options defined | 1 `ProductVariant`, 0 `VariantOptionValue` |
| VR4 | 1 variant, options defined, values aligned by index | 1 `ProductVariant`, option values linked via `VariantOptionValue` |
| VR5 | N variants, options defined | N `ProductVariant` rows, each linked to the correct indexed `ProductOptionValue` |
| VR6 | Variant with no `attributeValues` | No `ProductVariantAttributeValue` rows |
| VR7 | Variant with `attributeValues` | N `ProductVariantAttributeValue` rows per variant |
| VR8 | `variant.title = null/blank` | Title defaults to `PRODUCT_VARIANT_DEFAULT_TITLE` |
| VR9 | Variant with `mediaId` | `ProductVariant.mediaId` set; URL resolved in response |

---

### 1.7 Combined (Happy Path)

| # | Case | Expected |
|---|------|----------|
| C1 | Full product: category + brand + medias + options + attributes + variants with attributes | All entities saved correctly; full `ProductResponse` returned |
| C2 | Minimal product: name + slug only, everything else null/empty | Only base product saved; response has empty lists |

---

## 2. UPDATE PRODUCT — `PUT /products/{id}`

### 2.1 Validation / Error Cases

| # | Case | Expected Error |
|---|------|----------------|
| V1 | `productId` not found in DB | `PRODUCT_NOT_FOUND` |
| V2 | `name` changed to one that exists on another product | `PRODUCT_NAME_ALREADY_EXISTS` |
| V3 | `slug` changed to one that exists on another product | `PRODUCT_SLUG_ALREADY_EXISTS` |
| V4 | Same `name` / `slug` as current product (no conflict) | Success — uniqueness check excludes self |
| V5 | `categoryId` not found | `CATEGORY_NOT_FOUND` |
| V6 | `brandId` not found | `BRAND_NOT_FOUND` |
| V7 | Attribute ID not found in DB | `PRODUCT_ATTRIBUTE_NOT_FOUND` |
| V8 | Option ID not found in DB | `PRODUCT_OPTION_NOT_FOUND` |

---

### 2.2 Base Product Fields (Delta)

| # | Case | Expected |
|---|------|----------|
| B1 | Change `name` and `slug` | Product entity updated |
| B2 | `categoryId = null` (remove category) | Product `category` set to null |
| B3 | `categoryId` changed to another valid ID | New category resolved and applied |
| B4 | `brandId = null` (remove brand) | Product `brand` set to null |

---

### 2.3 Media (Delta)

| # | Case | Existing DB State | Expected |
|---|------|-------------------|----------|
| MD1 | `medias = null` or `[]` | 3 existing medias | All 3 medias deleted |
| MD2 | Same medias sent (by position/mediaId) | N existing medias | Existing updated, none deleted |
| MD3 | Add new media | N existing medias | New ones inserted |
| MD4 | Remove one media from list | N existing medias | Omitted one deleted, rest preserved |

---

### 2.4 Options (Delta)

| # | Case | Existing DB State | Expected |
|---|------|-------------------|----------|
| OD1 | `options = null` or `[]` | 2 combinations, 4 values | All combinations and values deleted |
| OD2 | Keep same options | N combinations | Positions updated, nothing deleted |
| OD3 | Add a new option | N combinations | New combination + values inserted |
| OD4 | Remove one option | N combinations | Omitted combination + its values deleted |
| OD5 | Update a value's text (with `id`) | N values | Existing value updated in-place |
| OD6 | Add a new value to an existing option (no `id`) | N values | New `ProductOptionValue` inserted |
| OD7 | Remove a value (not sent in request) | N values | Omitted value deleted |

---

### 2.5 Attributes (Delta)

| # | Case | Existing DB State | Expected |
|---|------|-------------------|----------|
| AD1 | `attributes = null` or `[]` | 3 existing attribute values | All 3 deleted |
| AD2 | Same attributes (same IDs) | N attributes | Values updated in-place |
| AD3 | Add a new attribute | N attributes | New `ProductAttributeValue` inserted |
| AD4 | Remove an attribute (omit from request) | N attributes | Omitted one deleted |
| AD5 | Change value of existing attribute | N attributes | Value updated in-place |

---

### 2.6 Variants (Delta)

#### 2.6.1 Variant Entity

| # | Case | Existing DB State | Expected |
|---|------|-------------------|----------|
| VD1 | `variants = null` or `[]` | 3 existing variants | No variant changes performed (`saveUpdatedVariants` returns early) |
| VD2 | Same variants (with IDs) | N variants | Existing variants updated via `applyVariant` |
| VD3 | Add a new variant (no `id` in request) | N variants | New `ProductVariant` inserted |
| VD4 | Remove a variant (omit from request) | N variants | Omitted variant deleted from DB |

#### 2.6.2 VariantOptionValue (Delta)

| # | Case | Existing DB State | Expected |
|---|------|-------------------|----------|
| VOV1 | No options defined; variants exist | 0 existing `VariantOptionValue` | No `VariantOptionValue` changes |
| VOV2 | Existing `VariantOptionValue` re-sent (matched by `variantId_povId` key) | N existing links | Links preserved, no new inserts |
| VOV3 | New option value assigned to variant (no existing link) | N existing links | New `VariantOptionValue` inserted |
| VOV4 | Option value removed from variant (not re-sent) | N existing links | Omitted `VariantOptionValue` deleted |
| VOV5 | Option combination removed entirely | N existing links | All `VariantOptionValue` for that option deleted |

#### 2.6.3 ProductVariantAttributeValue (Delta)

| # | Case | Existing DB State | Expected |
|---|------|-------------------|----------|
| VAV1 | No `attributeValues` for variant | N existing `ProductVariantAttributeValue` | None processed; omitted ones deleted |
| VAV2 | Same attributes re-sent (matched by `variantId_attributeId` key) | N existing | Updated in-place |
| VAV3 | New attribute added to variant | N existing | New `ProductVariantAttributeValue` inserted |
| VAV4 | Attribute removed from variant (omit from request) | N existing | Omitted one deleted |
| VAV5 | Attribute value text changed | N existing | Value updated in-place |

---

### 2.7 Combined (Happy Path)

| # | Case | Expected |
|---|------|----------|
| H1 | Full update: change name, add media, swap option, add attribute, add variant | All delta operations execute; final `ProductResponse` reflects new state |
| H2 | Update only base fields, keep all options/attributes/variants unchanged | Only product row updated; everything else preserved |
| H3 | Completely replace options: remove old, add new | Old combinations/values/variant links deleted; new ones inserted |

---

## 3. Option Combination Deletion Order (Important)

> [!IMPORTANT]
> `deleteOmittedOptionCombinationsAndValues()` is called **after** `saveUpdatedVariants()`.
> This ordering must be validated to avoid FK constraint violations.

| # | Case | Expected |
|---|------|----------|
| FK1 | Remove an option that still has `VariantOptionValue` referencing it | Variant option values cleaned up first → then option combination deleted → no FK violation |

---

## 4. Test Setup Requirements

| Prerequisite | Purpose |
|---|---------|
| At least 1 `ProductOption` in DB | Used by option combination tests |
| At least 1 `ProductAttribute` in DB | Used by attribute tests |
| At least 1 `Category` in DB | Used by category resolution tests |
| At least 1 `Brand` in DB | Used by brand resolution tests |
| At least 1 `Media` in DB (if media upload not mocked) | Used by media tests |
| A seeded product with full relations | Used as the base state for all UPDATE delta tests |
