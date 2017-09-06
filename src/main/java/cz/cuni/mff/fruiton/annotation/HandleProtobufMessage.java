package cz.cuni.mff.fruiton.annotation;

import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HandleProtobufMessage {

    WrapperMessage.MessageCase messageCase();

}
