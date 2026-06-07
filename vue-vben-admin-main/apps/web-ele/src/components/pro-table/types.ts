import type { Ref, VNode } from 'vue';

import type { TableColumnCtx } from 'element-plus/es/components/table/src/table-column/defaults';

import type { BreakPoint, Responsive } from '#/components/grid/types';

export interface EnumProps {
  children?: EnumProps[];
  disabled?: boolean;
  label?: string;
  tagType?: string;
  value?: any;
  [key: string]: any;
}

export type TypeProps = 'expand' | 'index' | 'radio' | 'selection' | 'sort';

export type SearchType =
  | 'cascader'
  | 'date-picker'
  | 'input'
  | 'input-number'
  | 'select'
  | 'select-v2'
  | 'slider'
  | 'switch'
  | 'time-picker'
  | 'time-select'
  | 'tree-select';

export interface SearchRenderScope {
  clearable: boolean;
  data: EnumProps[];
  options: EnumProps[];
  placeholder: string;
  searchParam: Record<string, any>;
}

export type SearchProps = {
  defaultValue?: any | Ref<any>;
  el?: SearchType;
  key?: string;
  label?: string;
  offset?: number;
  order?: number;
  props?: Record<string, any>;
  render?: (scope: SearchRenderScope) => VNode;
  span?: number;
  tooltip?: string;
} & Partial<Record<BreakPoint, Responsive>>;

export interface FieldNamesProps {
  children?: string;
  label: string;
  value: string;
}

export interface RenderScope<T = any> {
  $index: number;
  column: TableColumnCtx<any>;
  row: T;
  [key: string]: any;
}

export interface HeaderRenderScope {
  $index: number;
  column: TableColumnCtx<any>;
  [key: string]: any;
}

export interface ColumnProps<T = any> {
  _children?: ColumnProps<T>[];
  align?: 'center' | 'left' | 'right' | string;
  enum?: EnumProps[] | Ref<EnumProps[]> | ((params?: any) => Promise<any>);
  fieldNames?: FieldNamesProps;
  fixed?: boolean | 'left' | 'right';
  headerRender?: (scope: HeaderRenderScope) => VNode;
  isFilterEnum?: boolean | Ref<boolean>;
  isSetting?: boolean | Ref<boolean>;
  isShow?: boolean | Ref<boolean>;
  label?: string;
  minWidth?: number | string;
  prop?: string;
  render?: (scope: RenderScope<T>) => VNode | string;
  search?: SearchProps;
  showOverflowTooltip?: boolean;
  sortable?: boolean | string;
  tag?: boolean | Ref<boolean>;
  type?: TypeProps;
  width?: number | string;
  [key: string]: any;
}

export interface Pageable {
  pageNum: number;
  pageSize: number;
  total: number;
}

export interface TableState {
  pageable: Pageable;
  searchInitParam: Record<string, any>;
  searchParam: Record<string, any>;
  tableData: any[];
  totalParam: Record<string, any>;
}

export interface ProTableProps {
  border?: boolean;
  columns: ColumnProps[];
  data?: any[];
  dataCallback?: (data: any) => any;
  initParam?: Record<string, any>;
  pagination?: boolean;
  requestApi?: (params: any) => Promise<any>;
  requestAuto?: boolean;
  requestError?: (error: any) => void;
  rowKey?: string;
  searchCol?: number | Record<BreakPoint, number>;
  searchDefaultCollapsed?: boolean;
  title?: string;
  toolButton?: boolean | ('refresh' | 'search' | 'setting')[];
}

export interface ProTableInstance {
  clearSelection: () => void;
  element: any;
  enumMap: Map<string, Record<string, any>[]>;
  getTableList: () => Promise<void> | void;
  handleCurrentChange: (currentPage: number) => Promise<void> | void;
  handleSizeChange: (size: number) => Promise<void> | void;
  isSelected: boolean;
  pageable: Pageable;
  reset: () => Promise<void> | void;
  search: () => Promise<void> | void;
  searchInitParam: Record<string, any>;
  searchParam: Record<string, any>;
  selectedList: Record<string, any>[];
  selectedListIds: any[];
  tableData: any[];
}
