# Product API Refactoring Report

This document records the issues identified during code review of the Product Create & Update APIs and tracks their resolution status.

## Summary Table

| # | Severity | Category | Target / Location | Issue | Status |
|---|---|---|---|---|---|
| 1 | 🟡 Medium | UX/API | `ErrorCode`, `ProductServiceImpl` | Single error for name AND slug conflict — client can't distinguish | ✅ **Resolved** |
| 2 | 🔴 High | Bug | `ProductServiceImpl` | Potential NPE on `currentAttributes` in attribute clear path | ✅ **Resolved** |
| 3 | 🟡 Medium | Bug | `ProductServiceImpl` | `toMap` without merge function throws on duplicate attribute IDs | ✅ **Resolved** |
| 4 | 🟢 Low | Cleanup | `ProductServiceImpl` | Dead method `deleteOmittedOptionValues` never called | ✅ **Resolved** |
| 5 | 🟢 Low | Cleanup | `ProductServiceImpl` | Step numbering skips Step 5 in both create and update | ✅ **Resolved** |
| 6 | 🔴 High | Design | `ProductServiceImpl` | God class violates SRP; too much logic in one service | ✅ **Resolved** |
| 7 | 🟡 Medium | Design | `ProductVariant*Request`, `ProductVariantHelper` | `toEntity()` / `applyTo()` in DTOs violates AGENTS.md contract | ✅ **Resolved** |
| 8 | 🟡 Medium | Bug | `ProductOptionCombinationHelper` | NPE risk if `option` is null in `createProductOptionCombination` | ✅ **Resolved** |
| 9 | 🟢 Low | Docs | `ProductCreateRequest:19` | No contract documentation for optional `categoryId`/`brandId` | ⏳ Pending |
| 10 | 🟡 Medium | Performance | `ProductServiceImpl` | Individual `save()` in loop — should use batch `saveAll()` | ✅ **Resolved** |

---

## Detailed Resolutions for Medium & High Severity Issues

### 1. Distinct Error Codes for Product Name & Slug Conflicts (🟡 Medium)
- **Problem**: Both name and slug duplicates threw the generic `PRODUCT_ALREADY_EXISTS`, making it impossible for clients to distinguish the conflicting field.
- **Resolution**:
  - Added `PRODUCT_NAME_ALREADY_EXISTS("product_name_already_exists", "Product name already exists")` and `PRODUCT_SLUG_ALREADY_EXISTS("product_slug_already_exists", "Product slug already exists")` to `ErrorCode.java`.
  - Updated `validateProductRequest` to check `existsByName` / `existsByNameAndIdNot` and `existsBySlug` / `existsBySlugAndIdNot` independently with their respective error codes.

### 2. NPE Guard on Attribute Clear Path (🔴 High)
- **Problem**: When clearing attributes (`request.attributes()` is empty), `currentAttributes.size()` and `deleteAll(currentAttributes)` could throw a `NullPointerException` if `currentAttributes` was null.
- **Resolution**:
  - Added null and empty check `Objects.nonNull(currentAttributes) && !currentAttributes.isEmpty()` before calling `productAttributeValueRepository.deleteAll(currentAttributes)`.

### 3. Safe Collector Merge on Duplicate Attribute IDs (🟡 Medium)
- **Problem**: `currentAttributes.stream().collect(Collectors.toMap(...))` lacked a merge function, throwing `IllegalStateException` on duplicate attribute IDs.
- **Resolution**:
  - Provided duplicate key resolution `(e1, _) -> e1` in the `toMap` collector.

### 6. Decomposition of Monolithic `ProductServiceImpl` (🔴 High)
- **Problem**: `ProductServiceImpl` was an oversized class (~1300 lines) handling media deltas, option combinations, variant options/attributes, and response mapping, violating the Single Responsibility Principle (SRP).
- **Resolution**:
  - Extracted domain logic into dedicated Spring `@Component` helpers:
    - `ProductVariantHelper`: Creation, updating, and mapping of `ProductVariant`, `VariantOptionValue`, and `ProductVariantAttributeValue`.
    - `ProductMediaHelper`: Media entity creation, delta calculation, and media ID extraction.
    - `ProductOptionCombinationHelper`: Option combination & value construction, delta resolution, and hierarchical response DTO generation.
  - `ProductServiceImpl` now focuses solely on transaction management and service orchestration.

### 7. Decoupling DTOs from JPA Entities (🟡 Medium)
- **Problem**: `ProductVariantCreateRequest` and `ProductVariantUpdateRequest` contained `toEntity()` and `applyTo()` methods, tightly coupling API contracts with Hibernate entities and violating clean architecture guidelines in `AGENTS.md`.
- **Resolution**:
  - Removed `toEntity()` and `applyTo()` methods and JPA entity imports from the request records.
  - Relocated entity creation and mutation logic into `ProductVariantHelper`.

### 8. Null Safety in Option Combination Creation (🟡 Medium)
- **Problem**: `createProductOptionCombination` accessed `option.getId()` without verifying that `option` was non-null.
- **Resolution**:
  - Validated that `option != null` and `option.getId() != null` in `ProductOptionCombinationHelper`, throwing `ApplicationException(ErrorCode.PRODUCT_OPTION_NOT_FOUND)` if missing.

### 10. Batch Persistence for Variant Option Values (🟡 Medium)
- **Problem**: In `saveVariants` and `updateSingleVariantOptionValues`, `variantOptionValueRepository.save(...)` was called inside nested loops, causing N+1 database queries.
- **Resolution**:
  - Refactored logic to collect `VariantOptionValue` entities across iterations and persist them in batch using `variantOptionValueRepository.saveAll(...)`.
