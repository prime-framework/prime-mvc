/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.primeframework.mvc.domain.location;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

import static net.java.lang.StringTools.*;

/**
 * <p> This class is a simple address object. </p>
 *
 * @author Brian Pontarelli
 */
@Embeddable
@MappedSuperclass
public class Address implements Serializable {
  private final static int serialVersionUID = 1;

  @Column(length = 512)
  private String street;

  @Column(length = 512)
  private String street2;

  @Column()
  private String city;

  @Column()
  private String state;

  @Column()
  private String district;

  @Column()
  private String country;

  @Column(name = "postal_code")
  private String postalCode;

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getCity() {
    return city;
  }

  public String getStreet2() {
    return street2;
  }

  public void setStreet2(String street2) {
    this.street2 = street2;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getDistrict() {
    return district;
  }

  public void setDistrict(String district) {
    this.district = district;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  /**
   * Compares the address with the given object for equality. This comparison uses all of the fields in the comparison.
   * The nullable fields are only district, postal code, and state.
   *
   * @param o The object to compare.
   * @return True if the object is an address and is equal.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Address)) return false;

    Address address = (Address) o;

    return (city != null ? city.equals(address.city) : address.city == null) &&
      (country != null ? country.equals(address.country) : address.country == null) &&
      (district != null ? district.equals(address.district) : address.district == null) &&
      (postalCode != null ? postalCode.equals(address.postalCode) : address.postalCode == null) &&
      (state != null ? state.equals(address.state) : address.state == null) &&
      (street != null ? street.equals(address.street) : address.street == null) &&
      (street2 != null ? street2.equals(address.street2) : address.street2 == null);

  }

  /**
   * Generates a hash code using all the fields.
   *
   * @return The hash code.
   */
  @Override
  public int hashCode() {
    int result = street != null ? street.hashCode() : 0;
    result = 31 * result + (street2 != null ? street2.hashCode() : 0);
    result = 31 * result + (city != null ? city.hashCode() : 0);
    result = 31 * result + (state != null ? state.hashCode() : 0);
    result = 31 * result + (district != null ? district.hashCode() : 0);
    result = 31 * result + (country != null ? country.hashCode() : 0);
    result = 31 * result + (postalCode != null ? postalCode.hashCode() : 0);
    return result;
  }

  /**
   * @return True if this address has any fields with data, false otherwise.
   */
  public boolean isContainsData() {
    return !isTrimmedEmpty(street) || !isTrimmedEmpty(state) || !isTrimmedEmpty(district) ||
      !isTrimmedEmpty(city) || !isTrimmedEmpty(country) || !isTrimmedEmpty(postalCode);
  }

  /**
   * Returns a String suitable for debugging.
   *
   * @return The address.
   */
  public String toString() {
    return street + "\n" + city + ", " + state + " " + postalCode + "\n" + country;
  }
}