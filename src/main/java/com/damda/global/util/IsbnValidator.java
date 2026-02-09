package com.damda.global.util;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * ISBN 유효성 검사 어노테이션
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IsbnValidator.IsbnVerifier.class)
@Documented
public @interface IsbnValidator {
    String message() default "유효하지 않은 ISBN 형식입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    /**
     * ISBN Validator 구현체
     */
    class IsbnVerifier implements ConstraintValidator<com.damda.global.util.IsbnValidator, String> {

        @Override
        public boolean isValid(String isbn, ConstraintValidatorContext context) {
            if (isbn == null || isbn.isEmpty()) {
                return true; // null은 @NotBlank에서 처리
            }

            // 하이픈, 공백, 콜론, "ISBN" 접두사 제거
            String cleanIsbn = isbn.replaceAll("(?i)ISBN(?:-1[03])?:?\\s*", "")
                    .replaceAll("[\\s-]", "");

            // ISBN-10 또는 ISBN-13 체크
            return isValidIsbn10(cleanIsbn) || isValidIsbn13(cleanIsbn);
        }

        /**
         * ISBN-10 유효성 검사 (체크섬 포함)
         * 형식: 10자리 숫자, 마지막은 숫자 또는 X
         * 체크섬 알고리즘: (10-i) * digit의 합이 11로 나누어떨어져야 함
         */
        private boolean isValidIsbn10(String isbn) {
            if (!isbn.matches("^[0-9]{9}[0-9Xx]$")) {
                return false;
            }

            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += (isbn.charAt(i) - '0') * (10 - i);
            }

            char lastChar = isbn.charAt(9);
            sum += (lastChar == 'X' || lastChar == 'x') ? 10 : (lastChar - '0');

            return sum % 11 == 0;
        }

        /**
         * ISBN-13 유효성 검사 (체크섬 포함)
         * 형식: 978 또는 979로 시작하는 13자리 숫자
         * 체크섬 알고리즘: 홀수 자리는 1배, 짝수 자리는 3배의 합을 10으로 나눈 나머지
         */
        private boolean isValidIsbn13(String isbn) {
            if (!isbn.matches("^97[89][0-9]{10}$")) {
                return false;
            }

            int sum = 0;
            for (int i = 0; i < 12; i++) {
                int digit = isbn.charAt(i) - '0';
                sum += (i % 2 == 0) ? digit : digit * 3;
            }

            int checkDigit = (10 - (sum % 10)) % 10;
            return checkDigit == (isbn.charAt(12) - '0');
        }
    }
}
