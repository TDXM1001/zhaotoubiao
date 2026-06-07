import type { FieldNamesProps } from './types';

export interface NormalizedTableResponse<T = any> {
  list: T[];
  pageNum?: number;
  pageSize?: number;
  total: number;
}

export function normalizeTableResponse<T = any>(
  response: any,
): NormalizedTableResponse<T> {
  const source = response?.data ?? response;

  if (Array.isArray(source)) {
    return {
      list: source,
      total: source.length,
    };
  }

  const list = Array.isArray(source?.list) ? source.list : [];

  return {
    list,
    pageNum: source?.pageNum,
    pageSize: source?.pageSize,
    total: Number(source?.total ?? list.length),
  };
}

export function cleanTableParams<T extends Record<string, any>>(params: T) {
  const result: Record<string, any> = {};

  for (const [key, rawValue] of Object.entries(params)) {
    const value = typeof rawValue === 'string' ? rawValue.trim() : rawValue;

    if (value === undefined || value === null || value === '') {
      continue;
    }

    result[key] = value;
  }

  return result as Partial<T>;
}

export function formatTableValue(value: any) {
  if (Array.isArray(value)) {
    return value.length ? value.join(' / ') : '--';
  }

  return value ?? '--';
}

export function getRowValueByProp(row: Record<string, any>, prop: string) {
  if (!prop.includes('.')) {
    return row[prop];
  }

  return prop.split('.').reduce<any>((current, key) => {
    return current?.[key];
  }, row);
}

export function getColumnPropName(prop: string) {
  const parts = prop.split('.');
  return parts.at(-1) ?? prop;
}

export function findEnumItem(
  enumData: any[],
  value: any,
  valueField: string,
  childrenField: string,
): any {
  for (const item of enumData) {
    if (item?.[valueField] === value) {
      return item;
    }

    if (Array.isArray(item?.[childrenField])) {
      const child = findEnumItem(
        item[childrenField],
        value,
        valueField,
        childrenField,
      );

      if (child) {
        return child;
      }
    }
  }
}

export function filterEnum(
  value: any,
  enumData?: any[],
  fieldNames?: FieldNamesProps,
  type?: 'tag',
) {
  const labelField = fieldNames?.label ?? 'label';
  const valueField = fieldNames?.value ?? 'value';
  const childrenField = fieldNames?.children ?? 'children';

  if (!Array.isArray(enumData)) {
    return type === 'tag' ? '' : '--';
  }

  const item = findEnumItem(enumData, value, valueField, childrenField);

  if (type === 'tag') {
    return item?.tagType ?? '';
  }

  return item?.[labelField] ?? '--';
}
