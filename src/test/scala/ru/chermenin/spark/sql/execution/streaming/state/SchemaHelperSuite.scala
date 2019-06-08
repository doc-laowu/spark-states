package ru.chermenin.spark.sql.execution.streaming.state

import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.types._
import org.scalatest.FunSuite
import ru.chermenin.spark.sql.execution.streaming.state.SchemaHelper.SchemaEvolutionException

class SchemaHelperSuite extends FunSuite{

  def IRow(items:Any*): InternalRow = {
    InternalRow.fromSeq(items)
  }

  test("simple schema, no changes") {
    val srcSchema = StructType(Seq(StructField("a",IntegerType),StructField("b",StringType)))
    val srcRows = Seq(IRow(1,"test"))
    val tgtRows = SchemaHelper.schemaEvolution(srcRows.iterator, srcSchema, srcSchema).toSeq
    assert(srcRows==tgtRows)
  }

  test("simple schema, new field") {
    val srcSchema = StructType(Seq(StructField("a",IntegerType),StructField("b",StringType)))
    val srcRows = Seq(IRow(1,"test"))
    val tgtSchema = StructType(Seq(StructField("a",IntegerType),StructField("b",StringType),StructField("c",StringType)))
    val tgtRows = SchemaHelper.schemaEvolution(srcRows.iterator, srcSchema, tgtSchema).toSeq
    assert(srcRows.size==tgtRows.size)
    assert(tgtRows.head.numFields==tgtSchema.size)
  }

  test("simple schema, removed field") {
    val srcSchema = StructType(Seq(StructField("a",IntegerType),StructField("b",StringType)))
    val srcRows = Seq(IRow(1,"test"))
    val tgtSchema = StructType(Seq(StructField("a",IntegerType)))
    val tgtRows = SchemaHelper.schemaEvolution(srcRows.iterator, srcSchema, tgtSchema).toSeq
    assert(srcRows.size==tgtRows.size)
    assert(tgtRows.head.numFields==tgtSchema.size)
  }

  test("simple schema, new and removed field") {
    val srcSchema = StructType(Seq(StructField("a",IntegerType),StructField("b",StringType)))
    val srcRows = Seq(IRow(1,"test"))
    val tgtSchema = StructType(Seq(StructField("a",IntegerType),StructField("c",StringType)))
    val tgtRows = SchemaHelper.schemaEvolution(srcRows.iterator, srcSchema, tgtSchema).toSeq
    assert(srcRows.size==tgtRows.size)
    assert(tgtRows.head.numFields==tgtSchema.size)
  }

  test("simple schema, unsupported data type change") {
    val srcSchema = StructType(Seq(StructField("a",IntegerType),StructField("b",StringType)))
    val srcRows = Seq(IRow(1,"test"))
    val tgtSchema = StructType(Seq(StructField("a",IntegerType),StructField("b",DoubleType)))
    intercept[SchemaEvolutionException]{
      SchemaHelper.schemaEvolution(srcRows.iterator, srcSchema, tgtSchema).toSeq
    }
  }

  test("nested schema, no changes") {
    val srcSchema = StructType(Seq(StructField("a",IntegerType),StructField("nested",StructType(Seq(StructField("a1",IntegerType),StructField("b1",StringType))))))
    val srcRows = Seq(IRow(1, IRow(11,"test")))
    val tgtRows = SchemaHelper.schemaEvolution(srcRows.iterator, srcSchema, srcSchema).toSeq
    tgtRows.foreach(println)
    assert(srcRows==tgtRows)
  }

  test("nested schema, new field") {
    val srcSchema = StructType(Seq(StructField("a",IntegerType),StructField("nested",StructType(Seq(StructField("a1",IntegerType),StructField("b1",StringType))))))
    val srcRows = Seq(IRow(1, IRow(11,"test")))
    val tgtSchema = StructType(Seq(StructField("a",IntegerType),StructField("nested",StructType(Seq(StructField("a1",IntegerType),StructField("b1",StringType),StructField("c1",StringType))))))
    val tgtRows = SchemaHelper.schemaEvolution(srcRows.iterator, srcSchema, tgtSchema).toSeq
    val tgtRowsExpected = Seq(IRow(1, IRow(11,"test",null)))
    tgtRows.foreach(println)
    assert(tgtRows==tgtRowsExpected)
  }

  test("nested schema, removed field") {
    val srcSchema = StructType(Seq(StructField("a",IntegerType),StructField("nested",StructType(Seq(StructField("a1",IntegerType),StructField("b1",StringType))))))
    val srcRows = Seq(IRow(1, IRow(11,"test")))
    val tgtSchema = StructType(Seq(StructField("a",IntegerType),StructField("nested",StructType(Seq(StructField("a1",IntegerType))))))
    val tgtRows = SchemaHelper.schemaEvolution(srcRows.iterator, srcSchema, tgtSchema).toSeq
    val tgtRowsExpected = Seq(IRow(1, IRow(11)))
    tgtRows.foreach(println)
    assert(tgtRows==tgtRowsExpected)
  }

  test("nested schema, new and removed field") {
    val srcSchema = StructType(Seq(StructField("a",IntegerType),StructField("nested",StructType(Seq(StructField("a1",IntegerType),StructField("b1",StringType))))))
    val srcRows = Seq(IRow(1, IRow(11,"test")))
    val tgtSchema = StructType(Seq(StructField("a",IntegerType),StructField("nested",StructType(Seq(StructField("a1",IntegerType),StructField("c1",StringType))))))
    val tgtRows = SchemaHelper.schemaEvolution(srcRows.iterator, srcSchema, tgtSchema).toSeq
    val tgtRowsExpected = Seq(IRow(1, IRow(11,null)))
    tgtRows.foreach(println)
    assert(tgtRows==tgtRowsExpected)
  }

  test("nested schema, unsupported data type change") {
    val srcSchema = StructType(Seq(StructField("a",IntegerType),StructField("nested",StructType(Seq(StructField("a1",IntegerType),StructField("b1",StringType))))))
    val srcRows = Seq(IRow(1,"test"))
    val tgtSchema = StructType(Seq(StructField("a",IntegerType),StructField("nested",StructType(Seq(StructField("a1",IntegerType),StructField("b1",DoubleType))))))
    intercept[SchemaEvolutionException]{
      SchemaHelper.schemaEvolution(srcRows.iterator, srcSchema, tgtSchema).toSeq
    }
  }
}
