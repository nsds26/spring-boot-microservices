import { SettingOutlined } from "@ant-design/icons";
import { Form } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { useNotifications } from "../../hooks/useNotifications";
import { RoomInterface } from "../../interfaces/roomInterface";
import { api } from "../../service/api";
import DeleteItemModal from "../table/deleteItemModal";
import RightDrawer from "../table/rightDrawer";
import TableList from "../table/table";
import TableOptions from "../table/tableOptions";
import CreateRoom from "./createRoom";
import RoomForm from "./roomForm";
import RoomSchedulesList from "./roomSchedules";

export default function RoomList() {
	const [visibleSchedules, setVisibleSchedules] = useState(false);
	const [activeRoom, setActiveRoom] = useState<RoomInterface>();
	const [rooms, setRooms] = useState<RoomInterface[]>();

	const [visibleDelete, setDeleteVisible] = useState(false);
	const [visibleEdit, setEditVisible] = useState(false);

	// FIXME: Check if this extra loading are necessary:
	const [deleteLoading, setDeleteLoading] = useState(false);
	const [editLoading, setEditLoading] = useState(false);

	const [loading, setLoading] = useState(false);

	const notify = useNotifications();
	const [form] = Form.useForm();

	useEffect(() => {
		fetchData();
	}, []);

	const fetchData = async () => {
		setLoading(true);
		await api
			.get("/room/")
			.then((res) => {
				if (res.data.success) setRooms(res.data.data);
			})
			// .catch((err) => {
			// 	console.log(err);
			// })
			.finally(() => {
				setLoading(false);
			});
	};

	const handleDelete = async () => {
		setDeleteLoading(true);
		await api
			.delete(`/room/delete/${activeRoom?.id}`)
			// .then((res) => {
			// 	if (res.data.success) notify.success("Sala excluída com sucesso!");
			// 	else notify.error(res.data.errorMessage);
			// })
			.finally(() => {
				notify.success("Sala excluída com sucesso!");
				setDeleteVisible(false);
				setDeleteLoading(false);
				fetchData();
			});
	};

	const saveEdit = async (room: RoomInterface) => {
		setEditLoading(true);
		await api
			.put(`/room/update/${room?.id}`, room)
			.then((res) => {
				if (res.data.success) notify.success("Sala editada com sucesso!");
				else notify.error(res.data.errorMessage);
			})
			// .catch((err) => {
			// 	console.log(err);
			// })
			.finally(() => {
				setEditVisible(false);
				setEditLoading(false);
				fetchData();
			});
	};

	const columns: ColumnsType<RoomInterface> = [
		{
			key: "room_name",
			title: "Name",
			dataIndex: "name",
		},
		{
			key: "room_capacity",
			title: "Room capacity",
			dataIndex: "capacity",
			align: "right",
		},
		{
			key: "room_creationDate",
			title: "Created at",
			dataIndex: "creationDate",
			align: "right",
		},
		{
			key: "room_lastUpdate",
			title: "Last updated at",
			dataIndex: "lastUpdate",
			align: "right",
		},
		{
			key: "room_opt",
			title: <SettingOutlined />,
			align: "center",
			width: 60,
			dataIndex: "operation",
			render: (_, room: RoomInterface) => (
				<TableOptions
					isSelf={false}
					applyPermissions={false}
					handleDelete={() => {
						setDeleteVisible(true);
						setActiveRoom(room);
					}}
					handleEdit={() => {
						setEditVisible(true);
						setActiveRoom(room);
					}}
					handleSchedules={() => {
						setVisibleSchedules(true);
						setActiveRoom(room);
						// findSchedulesByRoom(room?.id);
					}}
				/>
			),
		},
	];

	return (
		<>
			<TableList
				panelTitle="Salas disponíveis"
				form={form}
				setEditLoading={setEditLoading}
				visibleEdit={visibleEdit}
				columns={columns}
				editLoading={editLoading}
				setEditVisible={setEditVisible}
				fetchData={fetchData}
				dataSource={rooms}
				loading={loading}
				addButton={<CreateRoom fetchTable={fetchData} />}
			>
				<RoomForm saveForm={saveEdit} form={form} text={activeRoom?.name || "Room"} room={activeRoom} loading={editLoading} setLoading={setEditLoading} />
			</TableList>
			<DeleteItemModal
				name={activeRoom?.name}
				handleOk={handleDelete}
				visible={visibleDelete}
				setVisible={setDeleteVisible}
				loading={deleteLoading}
				setLoading={setDeleteLoading}
			/>

			<RightDrawer
				title={"Agendamentos"}
				visible={visibleSchedules}
				setVisible={setVisibleSchedules}
				loading={loading}
				setLoading={setLoading}
				handleOk={() => setVisibleSchedules(false)}
				drawerWidth={720}
				okBtnName={"Ok"}
			>
				<RoomSchedulesList visible={visibleSchedules} setVisible={setVisibleSchedules} room={activeRoom} />
			</RightDrawer>
		</>
	);
}
