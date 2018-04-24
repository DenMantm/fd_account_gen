CREATE OR REPLACE PACKAGE BODY SYSIMP_UTIL.SIMP_ACCOUNT_GENERATION AS


PROCEDURE DROP_DB_LINK ( P_DB_LINK_NAME VARCHAR2 )
    IS
        V_COUNT NUMBER :=0;
    BEGIN
        IF P_DB_LINK_NAME IS NOT NULL
        THEN
            EXECUTE IMMEDIATE ('SELECT COUNT(1) FROM ALL_DB_LINKS WHERE DB_LINK  = ''' || P_DB_LINK_NAME || '''') INTO V_COUNT;

            IF V_COUNT = 1
            THEN
                EXECUTE IMMEDIATE( 'DROP DATABASE LINK ' || P_DB_LINK_NAME ) ;
            END IF;
        END IF;
END DROP_DB_LINK;

FUNCTION GET_MOST_USED_VALUE (	P_INST_NUMBER VARCHAR2,
									P_TBL_NAME VARCHAR2,
									P_COLUMN_NAME VARCHAR2 )
	RETURN VARCHAR2
	IS
		V_VALUE VARCHAR2(100);
		V_SQL VARCHAR2 ( 1000);
	BEGIN
	
		IF P_COLUMN_NAME = 'INSTITUTION_NUMBER' THEN
		V_VALUE := P_INST_NUMBER;
		ELSE
				V_SQL := 	'SELECT ' || P_COLUMN_NAME ||
							' FROM (  SELECT ' || P_COLUMN_NAME || ', COUNT(*) ' ||
									' FROM BW3.' || P_TBL_NAME ||
									' WHERE INSTITUTION_NUMBER = '''||P_INST_NUMBER||'''' ||
									' GROUP BY ' || P_COLUMN_NAME ||
									' ORDER BY 2 DESC )' ||
							' WHERE ROWNUM <2';
							EXECUTE IMMEDIATE V_SQL INTO V_VALUE;
			END IF;

		RETURN V_VALUE;
	END GET_MOST_USED_VALUE;


FUNCTION CREATE_DB_LINK( P_DB_LINK_NAME VARCHAR2,
                              P_DB_NAMESPACE VARCHAR2 )
    RETURN BOOLEAN
    AS
        V_SQL VARCHAR2(2000);
        V_COUNT NUMBER;
        V_CREATION_SUCCESS BOOLEAN:= FALSE;
    BEGIN
        IF P_DB_LINK_NAME IS NOT NULL
        THEN
            BEGIN
                V_SQL   := 'ALTER SESSION CLOSE DATABASE LINK ' || P_DB_LINK_NAME;
                EXECUTE IMMEDIATE (V_SQL);
            EXCEPTION WHEN OTHERS THEN
                NULL;
            END;
            
            V_SQL   := 'CREATE DATABASE LINK ' || P_DB_LINK_NAME ||' CONNECT TO DBA_JOBS_TEST IDENTIFIED BY "carlow" USING ''' || P_DB_NAMESPACE || '''';

            EXECUTE IMMEDIATE(V_SQL);
            
            IF P_DB_NAMESPACE != 'BW3_SC'
            THEN          
                V_SQL   := 'SELECT COUNT(1) FROM DUAL@' || P_DB_LINK_NAME;
                EXECUTE IMMEDIATE(V_SQL) INTO V_COUNT;
            END IF;
                     
            IF V_COUNT = 1
            THEN
                V_CREATION_SUCCESS := TRUE;

            END IF;
        
        END IF;
        
        RETURN V_CREATION_SUCCESS;
              
        EXCEPTION
        WHEN OTHERS THEN
            V_CREATION_SUCCESS := FALSE;
            
        RETURN V_CREATION_SUCCESS;
    END CREATE_DB_LINK;
		
		--WRAPPER FUNCTION
PROCEDURE GET_POSTING_METHOD_DB_LINK( C_TAB_INDEXES OUT SYS_REFCURSOR, P_INSTITUTION_NUMBER VARCHAR2, P_DB_NAME VARCHAR2) 
IS
V_IS_LINK_CREATED BOOLEAN;
BEGIN

	DROP_DB_LINK('ACCOUNT_UTILITY_LINK');
	
	--V_IS_LINK_CREATED := CREATE_DB_LINK( 'INSTITUTION_DOC_UTILITY_LINK', P_DB_NAME );
	
	IF CREATE_DB_LINK( 'ACCOUNT_UTILITY_LINK', P_DB_NAME ) THEN
		GET_POSTING_METHOD_LIST(C_TAB_INDEXES, P_INSTITUTION_NUMBER ,'@ACCOUNT_UTILITY_LINK' );
	END IF;


-- DROP_DB_LINK('INSTITUTION_DOC_UTILITY_LINK');
END GET_POSTING_METHOD_DB_LINK;
			
PROCEDURE GET_POSTING_METHOD_LIST(C_TAB_INDEXES OUT SYS_REFCURSOR,  P_INSTITUTION_NUMBER VARCHAR2,P_DB_LINK_NAME VARCHAR2)
IS
--P_DB_LINK_NAME VARCHAR2(200) := ''||P_DB_LINK_NAME||'';
V_SQL VARCHAR2(2000);

BEGIN

V_SQL := 'SELECT DISTINCT pm.POSTING_METHOD As "method", pt.POSTING_METHOD As "index" '|| 
				' FROM BW3.CBR_POSTING_INSTRUCTIONS'||P_DB_LINK_NAME||' pt, BW3.CHT_POSTING_TARIFF'||P_DB_LINK_NAME||'  pm '||
				'WHERE pt.POSTING_METHOD =  pm.INDEX_FIELD '||
				'AND LANGUAGE = ''USA'' AND pt.institution_number = '''||P_INSTITUTION_NUMBER||''' ORDER BY pm.POSTING_METHOD' ;

OPEN C_TAB_INDEXES FOR V_SQL;

END GET_POSTING_METHOD_LIST;



PROCEDURE GET_ACCOUNT_LIST_DB_LINK( C_TAB_INDEXES OUT SYS_REFCURSOR, P_POSTING_METHOD VARCHAR2, P_INSTITUTION_NUMBER VARCHAR2, P_DB_NAME VARCHAR2)
IS
V_IS_LINK_CREATED BOOLEAN;
V_SQL VARCHAR2(2000);
BEGIN

		DROP_DB_LINK('ACCOUNT_UTILITY_LINK');
	
	--V_IS_LINK_CREATED := CREATE_DB_LINK( 'INSTITUTION_DOC_UTILITY_LINK', P_DB_NAME );
	
	IF CREATE_DB_LINK( 'ACCOUNT_UTILITY_LINK', P_DB_NAME ) THEN
	
--	V_SQL := 'SELECT DISTINCT pm.account_type_id As "type", PM.ACCT_CURRENCY As "currency" FROM BW3.CBR_POSTING_INSTRUCTIONS@ACCOUNT_UTILITY_LINK pm '||
--'WHERE pm.POSTING_METHOD = (SELECT DISTINCT INDEX_FIELD FROM BW3.CHT_POSTING_TARIFF@ACCOUNT_UTILITY_LINK WHERE POSTING_METHOD = '''||P_POSTING_METHOD ||''')';

		V_SQL := 'SELECT DISTINCT pm.account_type_id AS "type_index", '||
							'PM.ACCT_CURRENCY AS "currency_index", '||
							'CASE (SELECT distinct create_on_demand FROM BW3.cbr_contract_acct_types where institution_number = '''||  
							 P_INSTITUTION_NUMBER  ||''' and account_type_id = pm.account_type_id and ROWNUM < 2) WHEN ''000'' THEN ''000'' ELSE ''001'' END AS "isOnDemand" , '||
							'cur.swift_code AS "currency", '||
							'acc.type_id AS "type", '||
							'CASE WHEN EXISTS( '||
							'SELECT DISTINCT xx.account_type_id AS "type_index", xx.acct_currency AS "currency_index" '||
							'FROM cbr_contract_acct_types@ACCOUNT_UTILITY_LINK xx '||
							'WHERE institution_number = '''||P_INSTITUTION_NUMBER||''' '||
							'AND xx.account_type_id = pm.account_type_id '||
							'AND xx.acct_currency = PM.ACCT_CURRENCY '||
							') '||
							'     THEN ''Yes'' '||
							'     ELSE ''No'' '||
							'   END AS "Exists" '||
							'FROM BW3.CBR_POSTING_INSTRUCTIONS@ACCOUNT_UTILITY_LINK pm, BW3.CHT_ACCOUNT_TYPE_ID@ACCOUNT_UTILITY_LINK acc, BW3.CHT_CURRENCY@ACCOUNT_UTILITY_LINK cur '||
							'WHERE PM.ACCT_CURRENCY = CUR.ISO_CODE '||
							'AND pm.account_type_id = acc.index_field '||
							'AND pm.POSTING_METHOD = '''||P_POSTING_METHOD||''' '||
							'AND pm.institution_number = ''' ||P_INSTITUTION_NUMBER ||''' '||
							'AND acc.LANGUAGE = ''USA'' '||
							'AND acc.institution_number = ''00000000'' ';
	OPEN C_TAB_INDEXES FOR V_SQL;
	
		-- GET_POSTING_METHOD_LIST(T_POSTING_METHOD_LIST, P_INSTITUTION_NUMBER ,'@ACCOUNT_UTILITY_LINK' );
		
	END IF;
	
END GET_ACCOUNT_LIST_DB_LINK;



-- Generating account for the given data

PROCEDURE GET_ACCOUNT_INSERTS( RETURN_INSERTS OUT VARCHAR2, ACCOUNT_NAME_INDEX VARCHAR2, ACCOUNT_CURRENCY VARCHAR2, ACCT_NUMBER VARCHAR2, GROUP_NUMBER VARCHAR2, P_INSTITUTION_NUMBER VARCHAR2, P_DB_NAME VARCHAR2)
IS
V_IS_LINK_CREATED BOOLEAN;
V_SQL VARCHAR2(2000);
V_ACCT_ROW bw3.cbr_contract_acct_types%ROWTYPE;
V_CAS_ACCT_ROW BW3.CAS_CLIENT_ACCOUNT%ROWTYPE;
V_CYCLE_ACCT_ROW BW3.CAS_CYCLE_BOOK_BALANCE%ROWTYPE;
V_ROWID VARCHAR2(50);
V_INSERT_STRING VARCHAR2(2000);
V_INSERT_STRING_CAS VARCHAR2(2000);
V_INSERT_STRING_CYCLE VARCHAR2(2000);
V_CURRENT_CYCLE_START VARCHAR2(100);
V_CURRENT_CYCLE_END VARCHAR2(100);

BEGIN

		DROP_DB_LINK('ACCOUNT_UTILITY_LINK');
	
	--V_IS_LINK_CREATED := CREATE_DB_LINK( 'INSTITUTION_DOC_UTILITY_LINK', P_DB_NAME );
	
	IF CREATE_DB_LINK( 'ACCOUNT_UTILITY_LINK', P_DB_NAME ) THEN
					
					
					
					-- Getting current cycle start
					V_CURRENT_CYCLE_START := GET_MOST_USED_VALUE (	P_INSTITUTION_NUMBER,
									'cbr_contract_acct_types',
									'CURRENT_CYCLE_START' );
									
					--Getting current cycle end
					V_CURRENT_CYCLE_END := GET_MOST_USED_VALUE (	P_INSTITUTION_NUMBER,
									'cbr_contract_acct_types',
									'CURRENT_CYCLE_END' );
	
	
				--getting the already existing similar account row and assembling most likley insertable valuses--
				SELECT * INTO V_ACCT_ROW FROM BW3.cbr_contract_acct_types WHERE institution_number = P_INSTITUTION_NUMBER AND account_type_id = ACCOUNT_NAME_INDEX AND ROWNUM <2;
				
				-- ################### NEED TO INSERT HERE LOGIC TO HANDLE CASE WHEN ACCOUNT IS NOT FOUND FOR THIS INSTITUTION ---- ##########
				

				-- DEBUG::
				--DBMS_OUTPUT.PUT_LINE('Inserts: ' || V_ACCT_ROW.ACCT_CURRENCY);
				
				--changing currency of the account
				V_ACCT_ROW.ACCT_CURRENCY := ACCOUNT_CURRENCY;
				V_ACCT_ROW.RECORD_DATE := TO_CHAR(SYSDATE,'yyyymmdd');
				V_ACCT_ROW.CURRENT_CYCLE_START := V_CURRENT_CYCLE_START;
				V_ACCT_ROW.CURRENT_CYCLE_END := V_CURRENT_CYCLE_END;
						-- INSERTING NEW ACCOUNT OT THE TABLE
						INSERT INTO BW3.cbr_contract_acct_types
						VALUES V_ACCT_ROW
						RETURNING ROWID INTO V_ROWID;

        
				V_INSERT_STRING := GEN_INSERTS_UTIL.GENERATE_INSERT_FROM_ROWID_STR ('cbr_contract_acct_types',
                                                V_ROWID);
			  
				-- If the account is mandatory generating extra inserts for the account::
				
				IF V_ACCT_ROW.CREATE_ON_DEMAND = '000' THEN
				
					-- RETURN_INSERTS := V_INSERT_STRING || '/n THIS ONE IS MANDATORY ACCOUNT: '|| ACCT_NUMBER || ' - ' || GROUP_NUMBER;
					
					-- Creating extra entries in 
					-- CAS_CYCLE_BOOK_BALANCE and CAS_CLIENT_ACCOUNT
			
					-- CAS CLIENT ACCOUNTS PART --
					select * INTO V_CAS_ACCT_ROW from CAS_CLIENT_ACCOUNT where client_number = institution_number and institution_number = P_INSTITUTION_NUMBER and ROWNUM <2;
					
					-- Modifieing values here::::
					V_CAS_ACCT_ROW.ACCT_NUMBER := ACCT_NUMBER;
					V_CAS_ACCT_ROW.GROUP_NUMBER := GROUP_NUMBER;
					V_CAS_ACCT_ROW.SERVICE_CONTRACT_ID := V_ACCT_ROW.SERVICE_CONTRACT_ID;
					V_CAS_ACCT_ROW.ACCOUNT_TYPE_ID := V_ACCT_ROW.ACCOUNT_TYPE_ID;
					V_CAS_ACCT_ROW.ACCT_CURRENCY := V_ACCT_ROW.ACCT_CURRENCY;
					V_CAS_ACCT_ROW.RECORD_DATE := V_ACCT_ROW.RECORD_DATE;
					V_CAS_ACCT_ROW.LAST_STATEMENT_DATE := '';
					V_CAS_ACCT_ROW.UNALLOCATED_CREDITS := '';
					
					--V_INSERT_STRING_CAS VARCHAR2(2000);
					--V_INSERT_STRING_CYCLE VARCHAR2(2000);
					
					INSERT INTO BW3.CAS_CLIENT_ACCOUNT
						VALUES V_CAS_ACCT_ROW
						RETURNING ROWID INTO V_ROWID;
					
					V_INSERT_STRING_CAS := GEN_INSERTS_UTIL.GENERATE_INSERT_FROM_ROWID_STR ('CAS_CLIENT_ACCOUNT',
                                                V_ROWID);
					
					
					RETURN_INSERTS := V_INSERT_STRING || CHR(13) || CHR(10) ||'-- ACCOUNT IS MANDATORY, Adding extra inserts for CAS_CLIENT_ACCOUNTS'|| CHR(13) || CHR(10) ||V_INSERT_STRING_CAS;
					
					
					
					-- CAS_CYCLE_BOOK_BALANCE PART....
					
					
					select * INTO V_CYCLE_ACCT_ROW from CAS_CYCLE_BOOK_BALANCE where institution_number = P_INSTITUTION_NUMBER and PROCESSING_STATUS = '004' and rownum < 2;
					
					--V_INSERT_STRING_CYCLE
					-- Transforming 
					V_CYCLE_ACCT_ROW.RECORD_DATE := TO_CHAR(SYSDATE,'yyyymmdd');
					V_CYCLE_ACCT_ROW.ACCT_NUMBER := ACCT_NUMBER;
					V_CYCLE_ACCT_ROW.ACCT_CURRENCY := V_ACCT_ROW.ACCT_CURRENCY;
					V_CYCLE_ACCT_ROW.LAST_AMENDMENT_DATE := TO_CHAR( TO_DATE(V_ACCT_ROW.CURRENT_CYCLE_START,'YYYYDDMM') - 1 ,'YYYYDDMM');
					V_CYCLE_ACCT_ROW.DATE_CYCLE_START := V_ACCT_ROW.CURRENT_CYCLE_START;
					V_CYCLE_ACCT_ROW.DATE_CYCLE_END := V_ACCT_ROW.CURRENT_CYCLE_END;
					V_CYCLE_ACCT_ROW.BEGIN_BALANCE := '';
					V_CYCLE_ACCT_ROW.CURRENT_BALANCE := '';
					 V_CYCLE_ACCT_ROW.DR_BALANCE_CASH         := '';            
					 V_CYCLE_ACCT_ROW.DR_BALANCE_RETAIL       := '';            
					 V_CYCLE_ACCT_ROW.DR_BALANCE_INTEREST     := '';            
					 V_CYCLE_ACCT_ROW.DR_BALANCE_CHARGES      := '';            
					 V_CYCLE_ACCT_ROW.CR_BALANCE_PAYMENTS     := '';            
					 V_CYCLE_ACCT_ROW.CR_BALANCE_REFUNDS      := '';            
					 V_CYCLE_ACCT_ROW.CR_BALANCE_INTEREST     := '';            
					 V_CYCLE_ACCT_ROW.CR_BALANCE_BONUS        := '';            
					 V_CYCLE_ACCT_ROW.AMOUNT_HIGH_BAL_DR      := '';            
					 V_CYCLE_ACCT_ROW.AMOUNT_LOW_BAL_DR       := '';            
					 V_CYCLE_ACCT_ROW.AMOUNT_HIGH_BAL_CR      := '';            
					 V_CYCLE_ACCT_ROW.AMOUNT_LOW_BAL_CR       := '';            
					 V_CYCLE_ACCT_ROW.NUMBER_TRAN_CHRG_DR     := '';            
					 V_CYCLE_ACCT_ROW.NUMBER_TRAN_CHRG_CR     := '';            
					 V_CYCLE_ACCT_ROW.NUMBER_TRAN_NONCHRG_DR  := '';            
					 V_CYCLE_ACCT_ROW.NUMBER_TRAN_NONCHRG_CR  := '';            
					 V_CYCLE_ACCT_ROW.TRAN_CHARGES            := '';            
					 V_CYCLE_ACCT_ROW.ACCRUED_CR              := '';            
					 V_CYCLE_ACCT_ROW.ACCRUED_DR              := '';            
					 V_CYCLE_ACCT_ROW.PENDING_AUTHS           := '';                
					 V_CYCLE_ACCT_ROW.STATEMENT_NUMBER        := '';            
					 V_CYCLE_ACCT_ROW.CURRENT_BALANCE_CASH    := '';            
					 V_CYCLE_ACCT_ROW.INSTALLMENT_BALANCE     := '';       
					
					
					
						INSERT INTO BW3.CAS_CYCLE_BOOK_BALANCE
						VALUES V_CYCLE_ACCT_ROW
						RETURNING ROWID INTO V_ROWID;
					
					V_INSERT_STRING_CYCLE := GEN_INSERTS_UTIL.GENERATE_INSERT_FROM_ROWID_STR ('CAS_CYCLE_BOOK_BALANCE',
                                                V_ROWID);
					
					
					
					
					RETURN_INSERTS := RETURN_INSERTS || CHR(13) || CHR(10) || '-- Adding entrie to CAS_CYCLE_BOOK_BALANCE ' || CHR(13) || CHR(10) || V_INSERT_STRING_CYCLE;
					
					
					
					ELSE
					RETURN_INSERTS := V_INSERT_STRING;
					
				END IF;
				
																								
				--DBMS_output.put_line('Debug_GAI'||V_INSERT_STRING);
				
				--RETURN_INSERTS := V_INSERT_STRING;
					
				ROLLBACK;
						
					--	IF 
						
				--DBMS_output.put_line(V_MY_ROW);


		
	END IF;
			
			-- In case if there are no similar accounts avalible
			EXCEPTION
				WHEN no_data_found THEN
				DBMS_OUTPUT.PUT_LINE('noRec');
				DBMS_OUTPUT.PUT_LINE (SQLERRM);
					V_INSERT_STRING := 'No records';
					 ROLLBACK;
				WHEN OTHERS THEN
				DBMS_OUTPUT.PUT_LINE('error');
				DBMS_OUTPUT.PUT_LINE (SQLERRM);
				V_INSERT_STRING := 'Other error';
					 ROLLBACK;
	
END GET_ACCOUNT_INSERTS;


PROCEDURE GET_NEXT_ACCOUNT_INDEX( ACCOUNT_NUMBER OUT VARCHAR2, GROUP_NUMBER OUT VARCHAR2, P_INSTITUTION_NUMBER VARCHAR2, P_DB_NAME VARCHAR2)
IS
V_IS_LINK_CREATED BOOLEAN;
V_SQL_ACCOUNT_NUMBER VARCHAR2(2000);
V_SQL_GROUP_NUMBER VARCHAR2(2000);

V_NEXT_ACCOUNT_NUMBER VARCHAR2(100);
V_NEXT_GROUP_NUMBER VARCHAR2(100);

BEGIN

		DROP_DB_LINK('ACCOUNT_UTILITY_LINK');
	
	--V_IS_LINK_CREATED := CREATE_DB_LINK( 'INSTITUTION_DOC_UTILITY_LINK', P_DB_NAME );
	
	IF CREATE_DB_LINK( 'ACCOUNT_UTILITY_LINK', P_DB_NAME ) THEN
					
		
		-- SELECT TO GET LAST MAX ACCOUNT NUMBER
		V_SQL_ACCOUNT_NUMBER := 'SELECT MAX(acct_number) from BW3.CAS_CLIENT_ACCOUNT@ACCOUNT_UTILITY_LINK WHERE ACCT_NUMBER LIKE '''|| P_INSTITUTION_NUMBER ||'%'|| '''';
		EXECUTE IMMEDIATE V_SQL_ACCOUNT_NUMBER INTO V_NEXT_ACCOUNT_NUMBER;
		ACCOUNT_NUMBER := V_NEXT_ACCOUNT_NUMBER;
		
		-- SELECT TO GET LAST MAX GROUP NUMBER
		V_SQL_GROUP_NUMBER := 'SELECT MAX(group_number) FROM BW3.CAS_CLIENT_ACCOUNT@ACCOUNT_UTILITY_LINK where institution_number = '''||P_INSTITUTION_NUMBER||'''';
		EXECUTE IMMEDIATE V_SQL_GROUP_NUMBER INTO V_NEXT_GROUP_NUMBER;
		GROUP_NUMBER := V_NEXT_GROUP_NUMBER;
		
	END IF;
			
			-- In case if there are no similar accounts avalible
			EXCEPTION
				WHEN no_data_found THEN
				DBMS_OUTPUT.PUT_LINE('noRec');
				DBMS_OUTPUT.PUT_LINE (SQLERRM);
					
					 ROLLBACK;
				WHEN OTHERS THEN
				DBMS_OUTPUT.PUT_LINE('error');
				DBMS_OUTPUT.PUT_LINE (SQLERRM);
				
					 ROLLBACK;
	
END GET_NEXT_ACCOUNT_INDEX;





PROCEDURE GET_INSTITUTION_LIST( C_TAB_INDEXES OUT SYS_REFCURSOR, P_DB_NAME VARCHAR2)
IS
	V_IS_LINK_CREATED BOOLEAN;
	V_SQL VARCHAR2(2000);
BEGIN

		DROP_DB_LINK('ACCOUNT_UTILITY_LINK');
	
	--V_IS_LINK_CREATED := CREATE_DB_LINK( 'INSTITUTION_DOC_UTILITY_LINK', P_DB_NAME );
	
	IF CREATE_DB_LINK( 'ACCOUNT_UTILITY_LINK', P_DB_NAME ) THEN
	
		V_SQL := 'SELECT institution_number, institution_name FROM SYS_INSTITUTION_LICENCE@ACCOUNT_UTILITY_LINK ' ||
						 'WHERE institution_number NOT IN (''00000001'',''00000002'',''00000006'')  ORDER BY 1';
		OPEN C_TAB_INDEXES FOR V_SQL;
	
		-- GET_POSTING_METHOD_LIST(T_POSTING_METHOD_LIST, P_INSTITUTION_NUMBER ,'@ACCOUNT_UTILITY_LINK' );
	
	END IF;
	
END GET_INSTITUTION_LIST;


END SIMP_ACCOUNT_GENERATION;
/