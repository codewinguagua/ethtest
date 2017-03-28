package com.pdxchg.coordinator;

import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;

import com.google.gson.Gson;
import com.pdxchg.crypto.JWTCrypto;
import com.pdxchg.token.IDToken_pojo;

public class Authenticator {

	/**
	 * authenticate a token, verify the required scope is in, then return the user
	 * 
	 * @param token
	 * @param scope
	 * @return
	 */
	public static String authn(String token, String scope) {
		String user = "";
		String value = "";
				 try {
					value = JWTCrypto.getInstance().decryptVerify(token).toJson();
				} catch (JoseException e) {
					e.printStackTrace();
				} catch (InvalidJwtException e) {
					e.printStackTrace();
				}
				System.out.println(value);
		//  {"pdx_type":"access_token","iss":"PDX","sub":"yang","aud":"pdx","iat":1456482202,"auth_time":1456482202,"nbf":1456482202,"owner_type":"user","exp":1456511002}
		  Gson gson = new Gson();
          IDToken_pojo pojo = gson.fromJson(value, IDToken_pojo.class);
		  user = pojo.getSub();
		
		return user;
	}
	public static void main(String[] args) {
		//String vale = "eyJhbGciOiJFQ0RILUVTK0ExMjhLVyIsImVuYyI6IkExMjhDQkMtSFMyNTYiLCJraWQiOiJFQ0NQMjU2LTIwMTYwMTExMTAwMjQwIiwiY3R5IjoiSldUIiwiZXBrIjp7Imt0eSI6IkVDIiwieCI6IjNReEp0SnNVVUlHdXlDVFVtX2drdkJLQml4QUcxVlJaMlJ4OWpmN2tuSHciLCJ5IjoiQkFIaXJKRVo3MWdSNmdHbFJ5emI3ZkF6NFZQNnhBTVhtYklQMDJDT3pfUSIsImNydiI6IlAtMjU2In19.bwm8hvz026It8kuXDAvqrdGtCSHQFMNPcnzWp4uSYe31F_eaC5Qo2w.Gs7LC4n8eMXpGXHJc-ZwZg.fh9vbRDmJtc-PMBdAd-pwyd5Z36YPE0R0wPp3cWE2naOyfx6JOlgVPJe44kW7fJp9T8VlvhnwEEtTVtBXulFLFZIWBx9J_Nhjp3-7_6dnpP2wstugQiWBlvwTit-YR9lvtFEejRJkFEtolpXcMSsFH5K7CdJVIgnqHfv6ICf8rpf9_tjEzxq_rcR3Jz_kzaAQZvHt9OfRckxrBpbhofV0iMZoip8WVFLrSdeXsZ1izOt-agxkSMGgXf-S1OGmvtf5jKTTe4x_-ncbh_nu6AbFM-RQIzoG53B2IL1EZkiBxXmQ4FI5pjCr5qt4cLbJD3JBqR8ZGjjMwrTUHNih1lS0YYBfH8gmnyqQHhl1ONMkbhvnOW8k0_pE_SNl5HcBHBixN2gbhzWygNy4EuXZl0soFoRoqy0oNHNZ2XEKn2Bz_lqbKF3aw_DVMWRlfncEXEj53UBD91oqq3OzHWOQInlOeUrrKuFQfkZPpnn8TBgGnaWUdfP5US13RPnIZ6P4HYkaHzKMbfx34dAPFwnK1ATSkeKIE3eihm9Mwpej7Vb9BU.7qsD6m8LWl_V8X-I0Pj2cQ";
		String value = " eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiQTEyOEtXIiwia2lkIjoiU1lNSzEyOC0yMDE2MDExMTEwMDI0MCIsImN0eSI6IkpXVCJ9.5nVRiUAnb3EEN0DFHbqoLUDFIPTDfOqZnh3HS83EfyfT2pnM2FRKkg.K6-CxmtdmCqnusmL320URw.E6QgmsZo2nxeO5t2VsSpdrCIfRX3o_NNThJxyGq2gj_WnvhHx2QUxlU9oR0ROgTUZc8CDrCM4VNDmM9ZCrJNB7qJgNBp_xwOj7a3AlDypSZ8Q4S8LH7HprcAcAXwMRxqls_d5YnXtRvmDa06oVhGDU4KQaCJL3eZvBy80n9-hytPq0AlVyWI95jqVsrNmBJMh_Lu8uva38xwqe7nWg3wplvabNDQL0hWBPka3YiOO_knobWT7Rj-WIc0wI93hQDFtPjjjeBsG3Vb4Tl69opQrG_VckGfKp2xj9sntKcGCRK4fQgfJG7ig7mZuXTk2YbMtuKNoK1e9IsMuM_fQd8uouJAdrEB7pEfUELdnR1VzuyM4okj_DhWdmO87c4z3MDTEmG575zdXsOHpZhgfcjxjQlqD4bdIPnmjQFRRaQAWuLJ2k9wrf0n8cC4p5Ei7cN8zypsdJr-SUahbi5YhX2BLbrrNOnMmHDG0J0Sdve8qPEgB8gYuL93T2MZhAg9YSKg.RJPXl-i_EMA3lAWRqOT-Ew";
		try {
			 value = JWTCrypto.getInstance().decryptVerify(value).toJson();
			System.out.println(value);
		} catch (JoseException e) {
			e.printStackTrace();
		} catch (InvalidJwtException e) {
			e.printStackTrace();
		}
		
		  Gson gson = new Gson();
          IDToken_pojo pojo = gson.fromJson(value, IDToken_pojo.class);
          String user = pojo.getSub();
          System.out.println(user);
	}
	
}
