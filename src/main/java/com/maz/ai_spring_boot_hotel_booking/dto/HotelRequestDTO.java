//***************************** public record version of HotelRequestDTO - Its immutable *****************************//

package com.maz.ai_spring_boot_hotel_booking.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating/updating a Hotel (JSON body of POST /hotels).
 *
 * Written as a Java record - the standard for DTOs:
 * - Immutable: fields cannot change after creation (thread-safe, no accidental mutation)
 * - Auto-generates: constructor, accessors (name(), location()), equals, hashCode, toString
 * - Jackson (JSON library) supports records natively for (de)serialization
 *
 * No id field: the database generates ids; clients must never supply them.
 *
 * Validation annotations run when the controller parameter is marked @Valid.
 * If validation fails, Spring returns 400 Bad Request before any business logic runs.
 */
public record HotelRequestDTO(

        // Must not be null and must contain at least one non-whitespace character
        @NotBlank(message = "Hotel name is required")
        String name,

        @NotBlank(message = "Hotel location is required")
        String location
) {
}




//***************************** public class version of HotelRequestDTO - Its mutable *****************************//
//package com.maz.ai_spring_boot_hotel_booking.dto;
//
//import jakarta.validation.constraints.NotBlank;
//
///**
// * Request DTO for creating/updating a Hotel (JSON body of POST /hotels).
// *
// * No id field: the database generates ids; clients must never supply them.
// *
// * Validation annotations run when the controller parameter is marked @Valid.
// * If validation fails, Spring returns 400 Bad Request before any business logic runs.
// */
//public class HotelRequestDTO {
//
//        /**
//         * @NotBlank = must not be null AND must contain at least one
//         * non-whitespace character ("", "   ", null all fail).
//         * The message is returned to the client in the error response.
//         */
//        @NotBlank(message = "Hotel name is required")
//        private String name;
//
//        @NotBlank(message = "Hotel location is required")
//        private String location;
//
//        /**
//         * Jackson (the JSON library) needs a no-arg constructor to deserialize
//         * the incoming JSON into this object.
//         */
//        public HotelRequestDTO() {
//        }
//
//        public HotelRequestDTO(String name, String location) {
//                this.name = name;
//                this.location = location;
//        }
//
//        public String getName() {
//                return name;
//        }
//
//        public void setName(String name) {
//                this.name = name;
//        }
//
//        public String getLocation() {
//                return location;
//        }
//
//        public void setLocation(String location) {
//                this.location = location;
//        }
//}
