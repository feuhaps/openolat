-- user view
create view o_bs_identity_short_v as (
   select
      ident.id as id_id,
      ident.name as id_name,
      ident.lastlogin as id_lastlogin,
      ident.status as id_status,
      us.user_id as us_id,
      p_firstname.propvalue as first_name,
      p_lastname.propvalue as last_name,
      p_email.propvalue as email
   from o_bs_identity ident
   inner join o_user us on (ident.fk_user_id = us.user_id)
   left join o_userproperty p_firstname on (us.user_id = p_firstname.fk_user_id and p_firstname.propName = 'firstName')
   left join o_userproperty p_lastname on (us.user_id = p_lastname.fk_user_id and p_lastname.propName = 'lastName')
   left join o_userproperty p_email on (us.user_id = p_email.fk_user_id and p_email.propName = 'email')
);

-- assessment tables
-- efficiency statments
create table o_as_eff_statement (
   id number(20) not null,
   version number(20) not null,
   lastmodified date,
   creationdate date,
   passed number,
   score float(4),
   total_nodes number(20),
   attempted_nodes number(20),
   passed_nodes number(20),
   course_title varchar(255 char),
   course_short_title varchar(128 char),
   course_repo_key number(20),
   statement_xml clob,
   fk_identity number(20),
   fk_resource_id number(20),
   CONSTRAINT u_o_as_eff_statement UNIQUE (fk_identity, fk_resource_id),
   primary key (id)
);
alter table o_as_eff_statement add constraint eff_statement_id_cstr foreign key (fk_identity) references o_bs_identity (id);
create index eff_statement_repo_key_idx on o_as_eff_statement (course_repo_key);

-- user to course informations (was property initial and recent launch dates)
create table o_as_user_course_infos (
   id number(20) not null,
   version number(20) not null,
   creationdate date,
   lastmodified date,
   initiallaunchdate date,
   recentlaunchdate date,
   visit number(20),
   timespend number(20),
   fk_identity number(20),
   fk_resource_id number(20),
   CONSTRAINT u_o_as_user_course_infos UNIQUE (fk_identity, fk_resource_id),
   primary key (id)
);
alter table o_as_user_course_infos add constraint user_course_infos_id_cstr foreign key (fk_identity) references o_bs_identity (id);
alter table o_as_user_course_infos add constraint user_course_infos_res_cstr foreign key (fk_resource_id) references o_olatresource (resource_id);


-- assessment results
-- help view
create view o_gp_contextresource_2_group_v as (
   select
      cg_bg2resource.groupcontextresource_id as groupcontextresource_id,
      cg_bgcontext.groupcontext_id as groupcontext_id,
      cg_bgroup.group_id as group_id,
      cg_bg2resource.oresource_id as oresource_id,
      cg_bgcontext.grouptype as grouptype,
      cg_bgcontext.defaultcontext as defaultcontext,
      cg_bgroup.groupname as groupname,
      cg_bgroup.fk_ownergroup as fk_ownergroup,
      cg_bgroup.fk_partipiciantgroup as fk_partipiciantgroup,
      cg_bgroup.fk_waitinggroup as fk_waitinggroup
   from o_gp_bgcontextresource_rel cg_bg2resource
   inner join o_gp_bgcontext cg_bgcontext on (cg_bg2resource.groupcontext_fk = cg_bgcontext.groupcontext_id)
   inner join o_gp_business cg_bgroup on (cg_bg2resource.groupcontext_fk = cg_bgroup.groupcontext_fk)
);


-- notifications for e-portfolio
create or replace view o_ep_notifications_struct_v as (
   select
      struct.structure_id as struct_id,
      struct.structure_type as struct_type,
      struct.title as struct_title,
      struct.fk_struct_root_id as struct_root_id,
      struct.fk_struct_root_map_id as struct_root_map_id,
      (case when struct.structure_type = 'page' then struct.structure_id else parent_struct.structure_id end) as page_key,
      struct_link.creationdate as creation_date
   from o_ep_struct_el struct
   inner join o_ep_struct_struct_link struct_link on (struct_link.fk_struct_child_id = struct.structure_id)
   inner join o_ep_struct_el parent_struct on (struct_link.fk_struct_parent_id = parent_struct.structure_id)
   where struct.structure_type = 'page' or parent_struct.structure_type = 'page'
);

create or replace view o_ep_notifications_art_v as (
   select
      artefact.artefact_id as artefact_id,
      artefact_link.link_id as link_id,
      artefact.title as artefact_title,
      (case when struct.structure_type = 'page' then struct.title else root_struct.title end ) as struct_title,
      struct.structure_type as struct_type,
      struct.structure_id as struct_id,
      root_struct.structure_id as struct_root_id,
      root_struct.structure_type as struct_root_type,
      struct.fk_struct_root_map_id as struct_root_map_id,
      (case when struct.structure_type = 'page' then struct.structure_id else root_struct.structure_id end ) as page_key,
      artefact_link.fk_auth_id as author_id,
      artefact_link.creationdate as creation_date
   from o_ep_struct_el struct
   inner join o_ep_struct_artefact_link artefact_link on (artefact_link.fk_struct_id = struct.structure_id)
   inner join o_ep_artefact artefact on (artefact_link.fk_artefact_id = artefact.artefact_id)
   left join o_ep_struct_el root_struct on (struct.fk_struct_root_id = root_struct.structure_id)
);

create or replace view o_ep_notifications_rating_v as (
   select
      urating.rating_id as rating_id,
      map.structure_id as map_id,
      map.title as map_title,
      cast(urating.ressubpath as number(20)) as page_key,
      page.title as page_title,
      urating.creator_id as author_id,
      urating.creationdate as creation_date,
      urating.lastmodified as last_modified 
   from o_userrating urating
   inner join o_olatresource rating_resource on (rating_resource.resid = urating.resid and rating_resource.resname = urating.resname)
   inner join o_ep_struct_el map on (map.fk_olatresource = rating_resource.resource_id)
   left join o_ep_struct_el page on (page.fk_struct_root_map_id = map.structure_id and page.structure_id = cast(urating.ressubpath as integer))
);


create or replace view o_ep_notifications_comment_v as (
   select
      ucomment.comment_id as comment_id,
      map.structure_id as map_id,
      map.title as map_title,
      cast(ucomment.ressubpath as number(20)) as page_key,
      page.title as page_title,
      ucomment.creator_id as author_id,
      ucomment.creationdate as creation_date
   from o_usercomment ucomment
   inner join o_olatresource comment_resource on (comment_resource.resid = ucomment.resid and comment_resource.resname = ucomment.resname)
   inner join o_ep_struct_el map on (map.fk_olatresource = comment_resource.resource_id)
   left join o_ep_struct_el page on (page.fk_struct_root_map_id = map.structure_id and page.structure_id = cast(ucomment.ressubpath as integer))
);

