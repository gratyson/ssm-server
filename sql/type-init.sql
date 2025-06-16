SET schema 'ssm';

-- Component Types
INSERT INTO component_type (id, name, encrypted, ql_name)
    VALUES ('website', 'Website', false, 'website');

INSERT INTO component_type (id, name, encrypted, ql_name)
    VALUES ('text_blob', 'Text', true, 'textBlob');

INSERT INTO component_type (id, name, encrypted, ql_name)
    VALUES ('company_name', 'Company Name', false, 'companyName');

INSERT INTO component_type (id, name, encrypted, ql_name)
    VALUES ('expiration_year', 'Expiration Year', true, 'expirationYear');

INSERT INTO component_type (id, name, encrypted, ql_name)
    VALUES ('expiration_month', 'Expiration Month', true, 'expirationMonth');

INSERT INTO component_type (id, name, encrypted, ql_name)
    VALUES ('security_code', 'Security Code', true, 'securityCode');

INSERT INTO component_type (id, name, encrypted, ql_name)
    VALUES ('card_number', 'Card Number', true, 'cardNumber');

INSERT INTO component_type (id, name, encrypted, ql_name)
    VALUES ('password', 'Password', true, 'password');

INSERT INTO component_type (id, name, encrypted, ql_name)
    VALUES ('username', 'Username', true, 'username');

INSERT INTO component_type (id, name, encrypted, ql_name)
    VALUES ('file_id', 'File ID', false, 'fileId');

INSERT INTO component_type (id, name, encrypted, ql_name)
    VALUES ('file_name', 'File Name', true, 'fileName');


-- Key types
INSERT INTO key_type(id, name, abbr)
    VALUES ('direct_lock', 'Direct Lock', 'Direct');

INSERT INTO key_type(id, name, abbr)
    VALUES ('indirect_lock', 'Indirect Lock', 'Indirect');

-- Secret Types
INSERT INTO secret_type(id, name, abbr)
    VALUES ('website_password', 'Website Password', 'Web Password');

INSERT INTO secret_type(id, name, abbr)
    VALUES ('credit_card', 'Credit Card', 'Credit Card');

INSERT INTO secret_type(id, name, abbr)
    VALUES ('text_blob', 'Text Blob', 'Text');

INSERT INTO secret_type(id, name, abbr)
    VALUES ('files', 'Files', 'Files');