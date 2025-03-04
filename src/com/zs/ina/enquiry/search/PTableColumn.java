/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zs.ina.enquiry.search;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableView;

/**
 *
 * @author 100018
 * @param <S>
 * @param <T>
 */
public class PTableColumn<S, T> extends javafx.scene.control.TableColumn<S, T> {
 
  private final DoubleProperty percentageWidth = new SimpleDoubleProperty(1);
 
    /**
     *
     */
    public PTableColumn() {
    tableViewProperty().addListener(new ChangeListener<TableView<S>>() {
 
      @Override
      public void changed(ObservableValue<? extends TableView<S>> ov, TableView<S> t, TableView<S> t1) {
        if(PTableColumn.this.prefWidthProperty().isBound()) {
          PTableColumn.this.prefWidthProperty().unbind();
        }
 
        PTableColumn.this.prefWidthProperty().bind(t1.widthProperty().multiply(percentageWidth));
      }
    });
  }
     
    /**
     *
     * @return
     */
    public final DoubleProperty percentageWidthProperty() {
    return this.percentageWidth;
  }
     
    /**
     *
     * @return
     */
    public final double getPercentageWidth() {
    return this.percentageWidthProperty().get();
  }
     
    /**
     *
     * @param value
     * @throws IllegalArgumentException
     */
    public final void setPercentageWidth(double value) throws IllegalArgumentException {
    if(value >= 0 && value <= 1) {
      this.percentageWidthProperty().set(value);
    } else {
      throw new IllegalArgumentException(String.format("The provided percentage width is not between 0.0 and 1.0. Value is: %1$s", value));
    }
  }
}