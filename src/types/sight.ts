/**
 * 准星类型定义
 */

/** 准星样式类型 */
export type SightType =
  | 'cross'        // 十字准星
  | 'dot'          // 单点准星
  | 'tactical'     // 战术准星
  | 'circle'       // 圆形准星
  | 'bracket'      // 方括号准星
  | 'chevron'      // V形准星
  | 'custom';      // 自定义准星

/** 准星配置接口 */
export interface SightConfig {
  /** 准星类型 */
  type: SightType;
  /** 准星颜色（十六进制） */
  color: string;
  /** 准星大小（px） */
  size: number;
  /** 准星粗细（px） */
  thickness: number;
  /** 是否显示中心点 */
  showDot: boolean;
  /** 中心点大小（px） */
  dotSize: number;
  /** 准星透明度（0-1） */
  opacity: number;
}

/** 默认准星配置 */
export const DEFAULT_SIGHT_CONFIG: SightConfig = {
  type: 'cross',
  color: '#00FF00',
  size: 20,
  thickness: 1,
  showDot: true,
  dotSize: 3,
  opacity: 0.8,
};

/** 预设准星类型 */
export type PresetSight = { id: string; name: string; config: SightConfig };

/** 预设准星样式 */
export const PRESET_SIGHTS: Array<PresetSight> = [
  {
    id: 'classic',
    name: '经典十字',
    config: { ...DEFAULT_SIGHT_CONFIG, type: 'cross' },
  },
  {
    id: 'tactical',
    name: '战术红点',
    config: { ...DEFAULT_SIGHT_CONFIG, type: 'tactical', color: '#FF0000' },
  },
  {
    id: 'dot',
    name: '单点',
    config: { ...DEFAULT_SIGHT_CONFIG, type: 'dot', color: '#FF0000' },
  },
  {
    id: 'circle',
    name: '圆形',
    config: { ...DEFAULT_SIGHT_CONFIG, type: 'circle', color: '#00FFFF' },
  },
  {
    id: 'bracket',
    name: '方括号',
    config: { ...DEFAULT_SIGHT_CONFIG, type: 'bracket', color: '#FFFF00' },
  },
  {
    id: 'chevron',
    name: '倒V',
    config: { ...DEFAULT_SIGHT_CONFIG, type: 'chevron', color: '#FF00FF' },
  },
  {
    id: 'large',
    name: '大型十字',
    config: { ...DEFAULT_SIGHT_CONFIG, type: 'cross', size: 40, thickness: 2 },
  },
  {
    id: 'small',
    name: '小型十字',
    config: { ...DEFAULT_SIGHT_CONFIG, type: 'cross', size: 12, thickness: 1 },
  },
];
