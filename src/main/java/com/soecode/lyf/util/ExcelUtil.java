package com.soecode.lyf.util;

import com.google.common.collect.Maps;
import com.soecode.lyf.annotation.ExcelVOAttribute;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cglib.core.ReflectUtils;

import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

/**
 * Excel
 *
 * @author 熊志星
 * @history 2019/3/18 新建
 * @since JDK1.7
 */
@Slf4j
public class ExcelUtil<T> {
    private final static String XLS = "xls";
    private final static String XLSX = "xlsx";

    private Class<T> clazz;
    private Map<ColInfo, PropertyDescriptor> propertyDescriptorMap;

    public ExcelUtil(Class<T> clazz) {
        this.clazz = clazz;
        propertyDescriptorMap = Maps.newHashMap();

        try {
            PropertyDescriptor[] propertyDescriptors = ReflectUtils.getBeanProperties(clazz);
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                String propertyName = propertyDescriptor.getName();
                log.debug("propertyName={}", propertyName);
                Field field = null;

                field = clazz.getDeclaredField(propertyName);

                if (field == null) {
                    continue;
                }
                // 将有注解的field存放到map中.
                if (field.isAnnotationPresent(ExcelVOAttribute.class)) {
                    ExcelVOAttribute attr = field
                            .getAnnotation(ExcelVOAttribute.class);
                    int col = getExcelCol(attr.column());// 获得列号
                    propertyDescriptorMap.put(
                            ColInfo.builder().col(col).name(attr.name()).prompt(attr.prompt()).combo(Arrays.asList(attr.combo())).isExport(attr.isExport()).build()
                            , propertyDescriptor);
                }
            }
        } catch (Exception e) {
            log.error("catch e exception",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean writeExcel(List<T> list, String sheetName, int sheetSize, String outPath) {
        try {
            File file = new File(outPath);
            if(file.exists()){
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            return writeExcel(list,sheetName,sheetSize,fileOutputStream);
        } catch (Exception e) {
            log.error("catch a exception",e);
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 对list数据源将其里面的数据导入到excel表单
     *
     * @param sheetName 工作表的名称
     * @param sheetSize 每个sheet中数据的行数,此数值必须小于65536
     * @param output    java输出流
     */
    public boolean writeExcel(List<T> list, String sheetName, int sheetSize, OutputStream output) {
        if(CollectionUtils.isEmpty(list)){
            return true;
        }

        HSSFWorkbook workbook = new HSSFWorkbook();// 产生工作薄对象

        // excel2003中每个sheet中最多有65536行,为避免产生错误所以加这个逻辑.
        if (sheetSize > 65536 || sheetSize < 1) {
            sheetSize = 65536;
        }
        long sheetNo = getSheetNum(list.size(), sheetSize);// 取出一共有多少个sheet.
        for (int index = 0; index < sheetNo; index++) {
            HSSFSheet sheet = workbook.createSheet();// 产生工作表对象
            workbook.setSheetName(index, sheetName + index);// 设置工作表的名称.
            HSSFRow row;

            row = sheet.createRow(0);// 产生一行
            // 写入各个字段的列头名称
            for (Map.Entry<ColInfo, PropertyDescriptor> entry : propertyDescriptorMap.entrySet()) {
                Integer col = entry.getKey().getCol();
                //创建单元格并设置类型与值
                createCellWithTypeAndVal(row,col, String.class,entry.getKey().getName());

                // 如果设置了提示信息则鼠标放上去提示.
                if (StringUtils.isNotBlank(entry.getKey().getPrompt())) {
                    setHSSFPrompt(sheet, "", entry.getKey().getPrompt(), 1, 100, col, col);// 这里默认设了2-101列提示.
                }
                // 如果设置了combo属性则本列只能选择不能输入
                if (CollectionUtils.isNotEmpty(entry.getKey().getCombo())) {
                    setHSSFValidation(sheet, entry.getKey().getCombo().toArray(new String[0]), 1, 100, col, col);// 这里默认设了2-101列只能选择不能输入.
                }
            }

            int startNo = index * sheetSize;
            int endNo = Math.min(startNo + sheetSize, list.size());
            // 写入各条记录,每条记录对应excel表中的一行
            for (int i = startNo; i < endNo; i++) {
                row = sheet.createRow(i + 1 - startNo);
                T vo = (T) list.get(i); // 得到导出对象.

                for (Map.Entry<ColInfo, PropertyDescriptor> entry : propertyDescriptorMap.entrySet()) {
                    try {
                        // 根据ExcelVOAttribute中设置情况决定是否导出,有些情况需要保持为空,希望用户填写这一列.
                        if (entry.getKey().isExport()) {
                            Object value = entry.getValue().getReadMethod().invoke(vo);
                            Integer col = entry.getKey().getCol();

                            //创建单元格并设置类型与值
                            createCellWithTypeAndVal(row,col, entry.getValue().getPropertyType(),value);
                        }
                    } catch (Exception e) {
                        log.error("catch a exception", e);
                        throw new RuntimeException(e.getMessage());
                    }
                }
            }

        }
        try {
            output.flush();
            workbook.write(output);
            output.close();
            return true;
        } catch (IOException e) {
            log.error("catch a exception.", e);
            return false;
        }
    }

    public List<T> readExcel(String sheetName, String filePath, ExcelType excelType) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        return this.readExcel(sheetName, fileInputStream, excelType);
    }

    public List<T> readExcel(String sheetName, InputStream input, ExcelType excelType) throws Exception {
        if(excelType == null){
            throw new RuntimeException("无法识别的文件类型");
        }

        List<T> list = new ArrayList<T>();
        try {
            Workbook workbook = getWorkbook(input, excelType);
            Sheet sheet = null;
            if (StringUtils.isNotBlank(sheetName.trim())) {
                sheet = workbook.getSheet(sheetName.trim());// 如果指定sheet名,则取指定sheet中的内容.
            }
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);// 如果传入的sheet名不存在则默认指向第1个sheet.
            }
            int rows = sheet.getPhysicalNumberOfRows();// 得到数据的行数
            if (rows > 0) {// 有数据时才处理
                for (int i = 1; i < rows; i++) {// 从第2行开始取数据,默认第一行是表头.
                    Iterator<Cell> iterator = sheet.getRow(i).iterator();// 得到一行中的所有单元格对象.
                    T entity = null;

                    int j = 0;
                    while (iterator.hasNext()) {
                        PropertyDescriptor propertyDescriptor = getPropertyDescriptor(j++);// 从map中得到对应列的field.
                        if (propertyDescriptor == null) {
                            continue;
                        }

                        Cell c = iterator.next();// 单元格中的内容.
                        Object val = getTypeVal(c, propertyDescriptor.getPropertyType());
                        if (val == null) {
                            continue;
                        }
                        entity = (entity == null ? clazz.newInstance() : entity);// 如果不存在实例则新建.

                        propertyDescriptor.getWriteMethod().invoke(entity, val);
                    }
                    if (entity != null) {
                        list.add(entity);
                    }
                }
            }
        } catch (Exception e) {
            log.error("catch a exception.", e);
            throw new RuntimeException(e.getMessage());
        }
        return list;
    }


    /**
     * 创建单元格并设置类型和值
     * @param row
     * @param col
     * @param proCls
     * @param val
     */
    private HSSFCell createCellWithTypeAndVal(HSSFRow row,Integer col,Class proCls,Object val) {
        HSSFCell cell = row.createCell(col);// 创建列
        //cell.setCellType(cellType);// 设置列中写入内容为String类型
        if(String.class == proCls || char.class == proCls || Character.class == proCls){
            cell.setCellValue(val.toString());// 写入列名
        }else if(Boolean.class == proCls || boolean.class == proCls){
            cell.setCellValue(Boolean.valueOf(val.toString()));// 写入列名
        }else if(Byte.class == proCls || byte.class == proCls ||
                Short.class == proCls || short.class == proCls ||
                Integer.class == proCls || int.class == proCls ||
                Long.class == proCls || long.class == proCls ||
                Float.class == proCls || float.class == proCls ||
                Double.class == proCls || double.class == proCls ||
                BigDecimal.class == proCls){
            cell.setCellValue(Double.valueOf(val.toString()));// 写入列名
        }else if(Date.class == proCls){
            cell.setCellValue((Date)val);// 写入列名
        }else if(Calendar.class == proCls){
            cell.setCellValue((Calendar) val);// 写入列名
        }else if(RichTextString.class == proCls){
            cell.setCellValue((RichTextString) val);// 写入列名
        }else{
            log.warn("未知的类型.col={},proCls={},val={}",col,proCls.getName(),val);
        }
        return cell;
    }

    private PropertyDescriptor getPropertyDescriptor(Integer col){
        if(col == null){
            return null;
        }
        for(Map.Entry<ColInfo,PropertyDescriptor> entry:propertyDescriptorMap.entrySet()){
            if(entry.getKey().getCol() == col){
                return entry.getValue();
            }
        }
        return null;
    }

    private long getSheetNum(long dataSize, long sheetSize) {
        if (dataSize % sheetSize == 0) {
            return dataSize / sheetSize;
        }

        return (dataSize / sheetSize) + 1;
    }

    private Workbook getWorkbook(InputStream inputStream, ExcelType excelType) throws IOException {
        Workbook workbook = null;
        if (XLS.equalsIgnoreCase(excelType.name())) {
            workbook = new HSSFWorkbook(inputStream);
        } else if (XLSX.equalsIgnoreCase(excelType.name())) {
            workbook = new XSSFWorkbook(inputStream);
        } else {
            log.error("can not recognized file type.fileType={}", excelType);
            throw new RuntimeException("无法识别的文件类型");
        }
        return workbook;
    }

    private Object getTypeVal(Cell c, Class proCls) {
        if (Boolean.class == proCls || boolean.class == proCls) {
            return c.getBooleanCellValue();
        } else if (Byte.class == proCls || byte.class == proCls) {
            return (byte)c.getNumericCellValue();
        } else if (Character.class == proCls || char.class == proCls) {
            return StringUtils.isBlank(c.getStringCellValue()) ? "" : c.getStringCellValue().charAt(0);
        } else if (Short.class == proCls || short.class == proCls) {
            return (short)c.getNumericCellValue();
        } else if (Integer.class == proCls || int.class == proCls) {
            return (int)c.getNumericCellValue();
        } else if (Long.class == proCls || long.class == proCls) {
            return (long)c.getNumericCellValue();
        } else if (Float.class == proCls || float.class == proCls) {
            return (float)c.getNumericCellValue();
        } else if (Double.class == proCls || double.class == proCls) {
            return c.getNumericCellValue();
        } else if (String.class == proCls) {
            return c.getStringCellValue();
        } else if (BigDecimal.class == proCls) {
            return new BigDecimal(c.getNumericCellValue());
        } else if (proCls.isEnum()) {
            //枚举
            return EnumUtils.getEnum(proCls, c.getStringCellValue());
        } else if (Date.class == proCls || Calendar.class == proCls) {
            return c.getDateCellValue();
        }
        log.warn("未知的类型.proCls={}",proCls.getName());
        return null;
    }

    /**
     * 将EXCEL中A,B,C,D,E列映射成0,1,2,3
     *
     * @param col
     */
    private static int getExcelCol(String col) {
        col = col.toUpperCase();
        // 从-1开始计算,字母重1开始运算。这种总数下来算数正好相同。
        int count = -1;
        char[] cs = col.toCharArray();
        for (int i = 0; i < cs.length; i++) {
            count += (cs[i] - 64) * Math.pow(26, cs.length - 1 - i);
        }
        return count;
    }

    /**
     * 设置单元格上提示
     *
     * @param sheet         要设置的sheet.
     * @param promptTitle   标题
     * @param promptContent 内容
     * @param firstRow      开始行
     * @param endRow        结束行
     * @param firstCol      开始列
     * @param endCol        结束列
     * @return 设置好的sheet.
     */
    public static HSSFSheet setHSSFPrompt(HSSFSheet sheet, String promptTitle,
                                          String promptContent, int firstRow, int endRow, int firstCol,
                                          int endCol) {
        // 构造constraint对象
        DVConstraint constraint = DVConstraint
                .createCustomFormulaConstraint("DD1");
        // 四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow,
                endRow, firstCol, endCol);
        // 数据有效性对象
        HSSFDataValidation data_validation_view = new HSSFDataValidation(
                regions, constraint);
        data_validation_view.createPromptBox(promptTitle, promptContent);
        sheet.addValidationData(data_validation_view);
        return sheet;
    }

    /**
     * 设置某些列的值只能输入预制的数据,显示下拉框.
     *
     * @param sheet    要设置的sheet.
     * @param textlist 下拉框显示的内容
     * @param firstRow 开始行
     * @param endRow   结束行
     * @param firstCol 开始列
     * @param endCol   结束列
     * @return 设置好的sheet.
     */
    public static HSSFSheet setHSSFValidation(HSSFSheet sheet,
                                              String[] textlist, int firstRow, int endRow, int firstCol,
                                              int endCol) {
        // 加载下拉列表内容
        DVConstraint constraint = DVConstraint
                .createExplicitListConstraint(textlist);
        // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow,
                endRow, firstCol, endCol);
        // 数据有效性对象
        HSSFDataValidation data_validation_list = new HSSFDataValidation(
                regions, constraint);
        sheet.addValidationData(data_validation_list);
        return sheet;
    }

    @Data
    @ToString(callSuper = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class ColInfo {
        //列号
        private Integer col;
        //列名
        private String name;
        //提示信息
        private String prompt;
        //下拉选列表内容
        private List<String> combo;
        //是否导出
        private boolean isExport;
    }

    static enum ExcelType{
        XLS("xls"),
        XLSX("xlsx");

        private String type;

        ExcelType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
