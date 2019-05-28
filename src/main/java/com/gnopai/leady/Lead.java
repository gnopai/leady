package com.gnopai.leady;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

import java.time.ZonedDateTime;

@Value
@Builder
@Wither
public class Lead {
    @SerializedName("_id") String id;
    String email;
    String firstName;
    String lastName;
    String address;
    ZonedDateTime entryDate;
}
